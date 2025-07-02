package com.example.features.users.models

import com.example.domain.models.User
import kotlinx.serialization.Serializable

/**
 * User response DTO
 */
@Serializable
data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String? = null,
    // Role-specific fields
    val regNo: String? = null,
    val employeeId: String? = null,
    val department: String? = null,
    val position: String? = null,
    val yearOfStudy: Int? = null
) {
    companion object {
        fun fromUser(
            user: User,
            regNo: String? = null,
            employeeId: String? = null,
            department: String? = null,
            position: String? = null,
            yearOfStudy: Int? = null
        ): UserResponseDto {
            return UserResponseDto(
                id = user.id.toString(),
                name = user.name,
                email = user.email,
                role = user.role.name,
                createdAt = user.createdAt.toString(),
                updatedAt = user.updatedAt?.toString(),
                regNo = regNo,
                employeeId = employeeId,
                department = department,
                position = position,
                yearOfStudy = yearOfStudy
            )
        }
    }
}

/**
 * Create user request DTO
 */
@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    // Role-specific fields
    val regNo: String? = null,
    val employeeId: String? = null,
    val department: String? = null,
    val yearOfStudy: Int? = null
)

/**
 * Update user request DTO
 */
@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val department: String? = null,
    val yearOfStudy: Int? = null
)