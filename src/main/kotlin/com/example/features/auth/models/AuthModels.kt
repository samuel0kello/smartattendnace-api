package com.example.features.auth.models

import com.example.domain.models.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base signup request interface
 */
@Serializable
sealed class SignUpRequest {
    abstract val name: String
    abstract val email: String
    abstract val password: String
    abstract val role: UserRole
}

/**
 * Student-specific signup request
 */
@Serializable
@SerialName("student")
data class StudentSignUpRequest(
    override val name: String,
    override val email: String,
    override val password: String,
    val regNo: String,
    val department: String? = null,
    val yearOfStudy: Int? = null,
    override val role: UserRole = UserRole.STUDENT
) : SignUpRequest()

/**
 * Lecturer-specific signup request
 */
@Serializable
@SerialName("lecturer")
data class LecturerSignUpRequest(
    override val name: String,
    override val email: String,
    override val password: String,
    val employeeId: String,
    val department: String? = null,
    override val role: UserRole = UserRole.LECTURER
) : SignUpRequest()

/**
 * Admin-specific signup request
 */
@Serializable
@SerialName("admin")
data class AdminSignUpRequest(
    override val name: String,
    override val email: String,
    override val password: String,
    val department: String? = null,
    override val role: UserRole = UserRole.ADMIN
) : SignUpRequest()

/**
 * Login request
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Refresh token request
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * Authentication response
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String
)


@Serializable
data class SignUpRequestDTO(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val regNo: String? = null,
    val employeeId: String? = null,
    val department: String? = null,
    val yearOfStudy: Int? = null
)

fun SignUpRequestDTO.toSignUpRequest(): SignUpRequest {
    return when (role.uppercase()) {
        "STUDENT" -> StudentSignUpRequest(
            name = name,
            email = email,
            password = password,
            regNo = regNo ?: throw IllegalArgumentException("Student registration number is required"),
            department = department,
            yearOfStudy = yearOfStudy,
            role = UserRole.STUDENT
        )
        "LECTURER" -> LecturerSignUpRequest(
            name = name,
            email = email,
            password = password,
            employeeId = employeeId ?: throw IllegalArgumentException("Lecturer employee ID is required"),
            department = department,
            role = UserRole.LECTURER
        )
        "ADMIN" -> AdminSignUpRequest(
            name = name,
            email = email,
            password = password,
            department = department,
            role = UserRole.ADMIN
        )
        else -> throw IllegalArgumentException("Invalid role: $role")
    }
}