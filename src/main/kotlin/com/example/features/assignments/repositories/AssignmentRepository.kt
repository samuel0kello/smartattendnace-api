package com.example.features.assignments.repositories

import com.example.domain.models.Assignment
import com.example.domain.repositories.BaseRepository
import java.util.UUID

/**
 * Assignment repository interface
 */
interface AssignmentRepository : BaseRepository<Assignment> {
    suspend fun getAssignmentsByCourse(courseId: UUID): List<Assignment>
    suspend fun getAssignmentsByLecturer(lecturerId: UUID): List<Assignment>
}