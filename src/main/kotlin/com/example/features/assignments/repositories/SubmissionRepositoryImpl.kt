package com.example.features.assignments.repositories

import com.example.di.dbQuery
import com.example.domain.models.Submission
import com.example.domain.tables.Submissions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SubmissionRepository
 */
@Singleton
class SubmissionRepositoryImpl @Inject constructor() : SubmissionRepository {
    
    override suspend fun getById(id: UUID): Submission? = dbQuery {
        Submissions.select { Submissions.id eq id }
            .map { it.toSubmission() }
            .singleOrNull()
    }
    
    override suspend fun getByAssignmentAndStudent(assignmentId: UUID, studentId: UUID): Submission? = dbQuery {
        Submissions.select {
            (Submissions.assignmentId eq assignmentId) and (Submissions.studentId eq studentId)
        }
        .map { it.toSubmission() }
        .singleOrNull()
    }
    
    override suspend fun getByAssignment(assignmentId: UUID): List<Submission> = dbQuery {
        Submissions.select { Submissions.assignmentId eq assignmentId }
            .orderBy(Submissions.submissionDate, SortOrder.DESC)
            .map { it.toSubmission() }
    }
    
    override suspend fun getByStudent(studentId: UUID): List<Submission> = dbQuery {
        Submissions.select { Submissions.studentId eq studentId }
            .orderBy(Submissions.submissionDate, SortOrder.DESC)
            .map { it.toSubmission() }
    }
    
    override suspend fun create(submission: Submission): Submission = dbQuery {
        val insertStatement = Submissions.insert {
            it[id] = submission.id
            it[studentId] = submission.studentId
            it[assignmentId] = submission.assignmentId
            it[fileUrl] = submission.fileUrl
            it[submissionDate] = submission.submissionDate
            it[grade] = submission.grade
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toSubmission()
            ?: throw Exception("Failed to insert submission")
    }
    
    override suspend fun update(submission: Submission): Boolean = dbQuery {
        val updatedRows = Submissions.update({ Submissions.id eq submission.id }) {
            it[fileUrl] = submission.fileUrl
            it[submissionDate] = submission.submissionDate
            it[grade] = submission.grade
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = Submissions.deleteWhere { Submissions.id eq id }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to Submission model
     */
    private fun ResultRow.toSubmission(): Submission = Submission(
        id = this[Submissions.id].value,
        studentId = this[Submissions.studentId].value,
        assignmentId = this[Submissions.assignmentId].value,
        fileUrl = this[Submissions.fileUrl],
        submissionDate = this[Submissions.submissionDate],
        grade = this[Submissions.grade]
    )
}