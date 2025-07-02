package com.example.features.assignments.repositories

import com.example.domain.models.Submission
import java.util.UUID

/**
 * Submission repository interface
 */
interface SubmissionRepository {
    suspend fun getById(id: UUID): Submission?
    suspend fun getByAssignmentAndStudent(assignmentId: UUID, studentId: UUID): Submission?
    suspend fun getByAssignment(assignmentId: UUID): List<Submission>
    suspend fun getByStudent(studentId: UUID): List<Submission>
    suspend fun create(submission: Submission): Submission
    suspend fun update(submission: Submission): Boolean
    suspend fun delete(id: UUID): Boolean
}