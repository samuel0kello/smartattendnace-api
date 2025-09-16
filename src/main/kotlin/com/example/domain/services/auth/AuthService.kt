package com.example.domain.services.auth

import com.example.infrastructure.database.entity.User
import com.example.infrastructure.database.entity.UserRole
import com.example.infrastructure.database.entity.Users
import com.example.domain.model.*
import com.example.domain.services.email.EmailVerificationService
import com.example.infrastructure.security.TokenProvider
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class AuthService(
    private val tokenProvider: TokenProvider,
    private val emailVerificationService: EmailVerificationService
) {

    @OptIn(ExperimentalTime::class)
    fun registerUser(request: UserRegistrationRequest, baseUrl: String): UserResponse {
        var createdUserResponse: UserResponse? = null
        var emailToNotify: String? = null
        var verificationToken: String? = null

        transaction {
            val existingUser = User.find { Users.email eq request.email }.firstOrNull()
            if (existingUser != null) {
                throw IllegalArgumentException("Email already registered")
            }

            // Assuming validateRegistrationRequest() is defined elsewhere
            // validateRegistrationRequest(request)

            val user = User.new {
                email = request.email
                passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
                firstName = request.firstName
                lastName = request.lastName
                role = UserRole.valueOf(request.role)

                employerId = when (role) {
                    UserRole.ADMIN, UserRole.LECTURER -> request.employerId ?: ""
                    else -> null
                }

                registrationNumber = when (role) {
                    UserRole.STUDENT -> request.registrationNumber ?: ""
                    else -> null
                }

                isActive = true
                createdAt = Clock.System.now()
                updatedAt = Clock.System.now()
            }

            val token = UUID.randomUUID().toString()
            user.emailVerificationToken = token
            user.emailVerificationTokenExpiry = Clock.System.now().plus(24.hours)

            emailToNotify = user.email
            verificationToken = token
            createdUserResponse = mapToUserResponse(user) // mapToUserResponse should convert Instant correctly
        }

        emailVerificationService.sendVerificationEmail(emailToNotify!!, verificationToken!!, baseUrl)

        return createdUserResponse!!
    }

    @OptIn(ExperimentalTime::class)
    fun verifyEmail(token: String?): Boolean {
        if (token.isNullOrBlank()) return false

        return transaction {
            val user = User.find { Users.emailVerificationToken eq token }.firstOrNull()
                ?: return@transaction false

            val expiry = user.emailVerificationTokenExpiry
            if (expiry == null || expiry < Clock.System.now()) {
                return@transaction false
            }

            user.apply {
                this.emailVerified = true
                this.isActive = true
                this.emailVerificationToken = null
                this.emailVerificationTokenExpiry = null
                this.updatedAt = Clock.System.now()
            }

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

    @OptIn(ExperimentalTime::class)
    fun resetPassword(email: String): PasswordResetResponse = transaction {
        val user = User.find { Users.email eq email }.firstOrNull()
            ?: throw IllegalArgumentException("Email not registered")

        val tempPassword = generateTemporaryPassword()

        user.passwordHash = BCrypt.hashpw(tempPassword, BCrypt.gensalt())
        user.updatedAt = Clock.System.now()

        return@transaction PasswordResetResponse(
            success = true,
            message = "Password has been reset. Check your email for the temporary password.",
        )
    }

    @OptIn(ExperimentalTime::class)
    fun changePassword(userId: UUID, request: ChangePasswordRequest): UserResponse = transaction {
        val user = User.findById(userId) ?: throw IllegalArgumentException("User not found")

        if (!BCrypt.checkpw(request.currentPassword, user.passwordHash)) {
            throw IllegalArgumentException("Incorrect old password")
        }

        user.passwordHash = BCrypt.hashpw(request.newPassword, BCrypt.gensalt())
        user.updatedAt = Clock.System.now()

        mapToUserResponse(user)
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