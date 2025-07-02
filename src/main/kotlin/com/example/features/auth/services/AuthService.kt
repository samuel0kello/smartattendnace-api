package com.example.features.auth.services

import com.example.common.exceptions.BadRequestException
import com.example.common.exceptions.ConflictException
import com.example.common.exceptions.UnauthorizedException
import com.example.domain.models.Staff
import com.example.domain.models.Student
import com.example.domain.models.User
import com.example.features.auth.models.*
import com.example.features.auth.repositories.AuthRepository
import com.example.features.users.repositories.StaffRepository
import com.example.features.users.repositories.StudentRepository
import com.example.features.users.repositories.UserRepository
import mu.KotlinLogging
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * Authentication service interface
 */
interface AuthService {
    suspend fun signup(request: SignUpRequest): User
    suspend fun login(request: LoginRequest): Pair<User, JwtService.TokenPair>
    suspend fun refreshToken(refreshToken: String): Pair<User, JwtService.TokenPair>
    suspend fun generateTokens(user: User): JwtService.TokenPair
}

/**
 * Implementation of authentication service
 */
@Singleton
class AuthServiceImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val staffRepository: StaffRepository,
    private val jwtService: JwtService
) : AuthService {

    /**
     * Register a new user
     */
    override suspend fun signup(request: SignUpRequest): User {
        logger.info { "Processing signup request for ${request.email} with role ${request.role}" }

        // Validate request data
        validateSignupRequest(request)

        // Check if email already exists
        if (authRepository.findUserByEmail(request.email) != null) {
            logger.warn { "Signup failed: Email already exists: ${request.email}" }
            throw ConflictException("A user with this email already exists")
        }

        // Create a base user
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = User(
            id = UUID.randomUUID(),
            name = request.name,
            email = request.email,
            password = hashedPassword,
            role = request.role,
            createdAt = Instant.now()
        )

        val createdUser = userRepository.create(user)
        logger.info { "Created user with ID: ${createdUser.id}" }

        // Create role-specific records
        when (request) {
            is StudentSignUpRequest -> {
                logger.debug { "Creating student record for user ${createdUser.id}" }
                if (studentRepository.findByRegNo(request.regNo) != null) {
                    // Rollback user creation if student data is invalid
                    userRepository.delete(createdUser.id)
                    logger.warn { "Signup failed: Registration number already exists: ${request.regNo}" }
                    throw ConflictException("A student with this registration number already exists")
                }

                // Create student record
                val student = Student(
                    userId = createdUser.id,
                    regNo = request.regNo,
                    department = request.department,
                    yearOfStudy = request.yearOfStudy
                )
                studentRepository.create(student)
                logger.info { "Created student record for user ${createdUser.id}" }
            }

            is LecturerSignUpRequest -> {
                logger.debug { "Creating lecturer record for user ${createdUser.id}" }

                // Validate lecturer-specific data
                if (staffRepository.findByEmployeeId(request.employeeId) != null) {
                    // Rollback user creation if lecturer data is invalid
                    userRepository.delete(createdUser.id)
                    logger.warn { "Signup failed: Employee ID already exists: ${request.employeeId}" }
                    throw ConflictException("A staff member with this employee ID already exists")
                }

                // Create staff record
                val staff = Staff(
                    userId = createdUser.id,
                    employeeId = request.employeeId,
                    department = request.department,
                    position = "Lecturer"
                )
                staffRepository.create(staff)
                logger.info { "Created lecturer record for user ${createdUser.id}" }
            }

            is AdminSignUpRequest -> {
                logger.debug { "Creating admin record for user ${createdUser.id}" }

                // Create admin staff record with generated employee ID
                val adminEmployeeId = "ADM-" + UUID.randomUUID().toString().substring(0, 8)
                val staff = Staff(
                    userId = createdUser.id,
                    employeeId = adminEmployeeId,
                    department = request.department,
                    position = "Administrator"
                )
                staffRepository.create(staff)
                logger.info { "Created admin record for user ${createdUser.id}" }
            }
        }

        return createdUser
    }

    /**
     * Authenticate a user and generate tokens
     */
    override suspend fun login(request: LoginRequest): Pair<User, JwtService.TokenPair> {
        logger.info { "Processing login request for ${request.email}" }

        // Find user by email
        val user = authRepository.findUserByEmail(request.email)
            ?: throw UnauthorizedException("Invalid email or password")

        // Verify password
        if (!BCrypt.checkpw(request.password, user.password)) {
            logger.warn { "Login failed: Invalid password for ${request.email}" }
            throw UnauthorizedException("Invalid email or password")
        }

        logger.info { "Login successful for user ${user.id}" }

        // Generate tokens
        val tokens = jwtService.generateTokens(user)
        return Pair(user, tokens)
    }

    /**
     * Refresh the access token using a valid refresh token
     */
    override suspend fun refreshToken(refreshToken: String): Pair<User, JwtService.TokenPair> {
        logger.info { "Processing token refresh request" }

        // Extract claims from refresh token
        val claims = jwtService.extractClaims(refreshToken)
            ?: throw UnauthorizedException("Invalid refresh token")

        // Find user by ID
        val user = userRepository.getById(UUID.fromString(claims.userId))
            ?: throw UnauthorizedException("User not found")

        logger.info { "Token refresh successful for user ${user.id}" }

        // Generate new tokens
        val tokens = jwtService.generateTokens(user)
        return Pair(user, tokens)
    }

    /**
     * Generate tokens for a user
     */
    override suspend fun generateTokens(user: User): JwtService.TokenPair {
        return jwtService.generateTokens(user)
    }

    /**
     * Validate signup request data
     */
    private fun validateSignupRequest(request: SignUpRequest) {
        // Validate common fields
        if (request.name.isBlank()) {
            throw BadRequestException("Name cannot be blank")
        }

        if (request.email.isBlank() || !isValidEmail(request.email)) {
            throw BadRequestException("Valid email address is required")
        }

        if (request.password.length < 6) {
            throw BadRequestException("Password must be at least 6 characters long")
        }

        // Validate role-specific fields
        when (request) {
            is StudentSignUpRequest -> {
                if (request.regNo.isBlank()) {
                    throw BadRequestException("Registration number is required for students")
                }
            }

            is LecturerSignUpRequest -> {
                if (request.employeeId.isBlank()) {
                    throw BadRequestException("Employee ID is required for lecturers")
                }
            }

            is AdminSignUpRequest -> {
                // No additional validation needed for admins
            }
        }
    }

    /**
     * Simple email validation
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }
}
