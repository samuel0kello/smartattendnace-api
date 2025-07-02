package com.example.features.assignments.repositories

import com.example.domain.models.Grade
import java.util.UUID

/**
 * Grade repository interface
 */
interface GradeRepository {
    suspend fun getById(id: UUID): Grade?
    suspend fun getByAssignmentAndStudent(assignmentId: UUID, studentId: UUID): Grade?
    suspend fun getByAssignment(assignmentId: UUID): List<Grade>
    suspend fun getByStudent(studentId: UUID): List<Grade>
    suspend fun create(grade: Grade): Grade
    suspend fun update(grade: Grade): Boolean
    suspend fun delete(id: UUID): Boolean
}