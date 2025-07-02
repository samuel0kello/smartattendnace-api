`package com.example.domain.models

import java.time.Instant
import java.util.UUID

/**
 * User roles in the system
 */
enum class UserRole {
    STUDENT, 
    LECTURER, 
    ADMIN
}

/**
 * User entity
 */
data class User(
    override val id: UUID,
    val name: String,
    val email: String,
    val password: String, // Hashed password
    val role: UserRole,
    val createdAt: Instant,
    val updatedAt: Instant? = null
) : BaseEntity

/**
 * Student entity (extends User)
 */
data class Student(
    val userId: UUID,
    val regNo: String,
    val department: String? = null,
    val yearOfStudy: Int? = null
)

/**
 * Staff entity (extends User for Lecturers and Admins)
 */
data class Staff(
    val userId: UUID,
    val employeeId: String,
    val department: String? = null,
    val position: String? = null
)