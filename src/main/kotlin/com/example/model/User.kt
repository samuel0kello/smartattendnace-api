package com.example.model

import com.example.database.entity.UserRole

data class User(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val employerId: String? = null,
    val registrationNumber: String? = null,
    val profilePicture: String? = null,
    // Not included in responses, only for authentication
    val password: String? = null
) {
    // Helper properties to check a user type
    val isAdmin get() = role == UserRole.ADMIN
    val isLecturer get() = role == UserRole.LECTURER
    val isStudent get() = role == UserRole.STUDENT
}

data class PostUserBody(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val employerId: String? = null,
    val registrationNumber: String? = null
)

data class PutUserBody(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String? = null,
    val employerId: String? = null,
    val registrationNumber: String? = null,
    val profilePicture: String? = null
)