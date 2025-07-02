package com.example.features.users.repositories

import com.example.domain.models.Staff
import java.util.UUID

/**
 * Staff repository interface
 */
interface StaffRepository {
    suspend fun findByUserId(userId: UUID): Staff?
    suspend fun findByEmployeeId(employeeId: String): Staff?
    suspend fun create(staff: Staff): Staff
    suspend fun update(staff: Staff): Boolean
    suspend fun delete(userId: UUID): Boolean
}