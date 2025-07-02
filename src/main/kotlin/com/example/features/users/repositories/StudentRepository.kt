package com.example.features.users.repositories

import com.example.domain.models.Student
import java.util.UUID

/**
 * Student repository interface
 */
interface StudentRepository {
    suspend fun findByUserId(userId: UUID): Student?
    suspend fun findByRegNo(regNo: String): Student?
    suspend fun create(student: Student): Student
    suspend fun update(student: Student): Boolean
    suspend fun delete(userId: UUID): Boolean
}
