package com.example.services.auth

import com.example.database.entity.User
import com.example.database.entity.UserRole
import com.example.database.entity.Users
import com.example.model.*
import com.example.services.email.EmailVerificationService
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

class AuthService (
    private val tokenProvider: TokenProvider,
    private val emailVerificationService: EmailVerificationService
){

    fun registerUser(request: UserRegistrationRequest, baseUrl: String): UserResponse {
        // Hold values produced inside the transaction so we can send email after commit
        var createdUserResponse: UserResponse? = null
        var emailToNotify: String? = null
        var verificationToken: String? = null

        transaction {
            // Check if email already exists
            val existingUser = User.find { Users.email eq request.email }.firstOrNull()
            if (existingUser != null) {
                throw IllegalArgumentException("Email already registered")
            }

            validateRegistrationRequest(request)

            val user = User.new {
                email = request.email
                passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
                firstName = request.firstName
                lastName = request.lastName
                role = UserRole.valueOf(request.role)

                // Set employerId based on role
                employerId = when (role) {
                    UserRole.ADMIN, UserRole.LECTURER -> request.employerId ?: ""
                    else -> ""
                }

                // Set registrationNumber based on role
                registrationNumber = when (role) {
                    UserRole.STUDENT -> request.registrationNumber ?: ""
                    else -> ""
                }

                isActive = true
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }

            // Generate and persist verification token inside the transaction
            val token = UUID.randomUUID().toString()
            user.emailVerificationToken = token
            user.emailVerificationTokenExpiry = LocalDateTime.now().plusHours(24)
            // Exposed will persist these changes on transaction commit

            // Capture values to use after the transaction
            emailToNotify = user.email
            verificationToken = token
            createdUserResponse = mapToUserResponse(user)
        } // transaction committed here

        // Send email after transaction commits (avoid network IO inside transaction)
        // These non-null assertions are safe because values were assigned inside the transaction above.
        emailVerificationService.sendVerificationEmail(emailToNotify!!, verificationToken!!, baseUrl)

        return createdUserResponse!!
    }

    fun verifyEmail(token: String?): Boolean {
        if (token.isNullOrBlank()) return false

        return transaction {
            val user = User.find { Users.emailVerificationToken eq token }.firstOrNull()
                ?: return@transaction false

            // Check expiry
            val expiry = user.emailVerificationTokenExpiry
            if (expiry == null || expiry.isBefore(LocalDateTime.now())) {
                return@transaction false
            }

            user.apply {
                this.emailVerified = true
                this.isActive = true
                this.emailVerificationToken = null
                this.emailVerificationTokenExpiry = null
                this.updatedAt = LocalDateTime.now()
            }

            // persist occurs on transaction commit
            true
        }
    }

    fun login(credentials: LoginCredentials): LoginTokenResponse = transaction {
        val user = User.find { Users.email eq credentials.email }.firstOrNull()
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!BCrypt.checkpw(credentials.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        if (!user.isActive) {
            throw IllegalArgumentException("Account is deactivated")
        }

        val token = tokenProvider.createToken(user)
        return@transaction LoginTokenResponse(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken
        )
    }

    fun refreshToken(request: RefreshTokenRequest): AccessTokenResponse = transaction {
        val userId = tokenProvider.verifyToken(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        val user = User.findById(UUID.fromString(userId))
            ?: throw IllegalArgumentException("User not found")

        if (!user.isActive) {
            throw IllegalArgumentException("Account is deactivated")
        }

        val accessToken = tokenProvider.createAccessToken(user)

        return@transaction AccessTokenResponse(accessToken)
    }

    fun resetPassword(email: String): PasswordResetResponse = transaction {
        val user = User.find { Users.email eq email }.firstOrNull()
            ?: throw IllegalArgumentException("Email not registered")

        // Generate temporary password
        val tempPassword = generateTemporaryPassword()

        // Update user with new password
        user.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
        user.updatedAt = LocalDateTime.now()

        // In a real application, you would send an email with the temporary password
        // For now, we'll just return it in the response (not secure for production)
        return@transaction PasswordResetResponse(
            success = true,
            message = "Password has been reset. Check your email for the temporary password.",
        )
    }

    fun changePassword(userId: UUID, request: ChangePasswordRequest): UserResponse = transaction {
        val user = User.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        // Verify current password
        if (!BCrypt.checkpw(request.currentPassword, user.passwordHash)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Update with new password
        user.passwordHash = BCrypt.hashpw(request.newPassword, BCrypt.gensalt())
        user.updatedAt = LocalDateTime.now()

        return@transaction mapToUserResponse(user)
    }

    private fun validateRegistrationRequest(request: UserRegistrationRequest) {
        // Basic validation
        if (request.email.isBlank() || request.password.isBlank() ||
            request.firstName.isBlank() || request.lastName.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }

        // Password strength check (add more rules as needed)
        if (request.password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        // Role-specific validation
        when (request.role) {
            "ADMIN", "LECTURER" -> {
                if (request.employerId.isNullOrBlank()) {
                    throw IllegalArgumentException("Employer ID is required for ${request.role} role")
                }
            }
            "STUDENT" -> {
                if (request.registrationNumber.isNullOrBlank()) {
                    throw IllegalArgumentException("Registration number is required for Student role")
                }
            }
            else -> throw IllegalArgumentException("Invalid role: ${request.role}")
        }
    }

    private fun generateTemporaryPassword(length: Int = 10): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    // Helper function to map User entity to UserResponse
    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id.value.toString(),
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role.name,
            employerId = user.employerId.takeIf { it?.isNotBlank() == true },
            registrationNumber = user.registrationNumber.takeIf { it?.isNotBlank() == true },
            profilePicture = user.profilePicture
        )
    }
}