package com.example.features.assignments.repositories

import com.example.di.dbQuery
import com.example.domain.models.Grade
import com.example.domain.tables.Grades
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GradeRepository
 */
@Singleton
class GradeRepositoryImpl @Inject constructor() : GradeRepository {
    
    override suspend fun getById(id: UUID): Grade? = dbQuery {
        Grades.select { Grades.id eq id }
            .map { it.toGrade() }
            .singleOrNull()
    }
    
    override suspend fun getByAssignmentAndStudent(assignmentId: UUID, studentId: UUID): Grade? = dbQuery {
        Grades.select {
            (Grades.assignmentId eq assignmentId) and (Grades.studentId eq studentId)
        }
        .map { it.toGrade() }
        .singleOrNull()
    }
    
    override suspend fun getByAssignment(assignmentId: UUID): List<Grade> = dbQuery {
        Grades.select { Grades.assignmentId eq assignmentId }
            .orderBy(Grades.gradedAt, SortOrder.DESC)
            .map { it.toGrade() }
    }
    
    override suspend fun getByStudent(studentId: UUID): List<Grade> = dbQuery {
        Grades.select { Grades.studentId eq studentId }
            .orderBy(Grades.gradedAt, SortOrder.DESC)
            .map { it.toGrade() }
    }
    
    override suspend fun create(grade: Grade): Grade = dbQuery {
        val insertStatement = Grades.insert {
            it[id] = grade.id
            it[studentId] = grade.studentId
            it[assignmentId] = grade.assignmentId
            it[score] = grade.score
            it[feedback] = grade.feedback
            it[gradedAt] = grade.gradedAt
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toGrade()
            ?: throw Exception("Failed to insert grade")
    }
    
    override suspend fun update(grade: Grade): Boolean = dbQuery {
        val updatedRows = Grades.update({ Grades.id eq grade.id }) {
            it[score] = grade.score
            it[feedback] = grade.feedback
            it[gradedAt] = grade.gradedAt
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = Grades.deleteWhere { Grades.id eq id }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to Grade model
     */
    private fun ResultRow.toGrade(): Grade = Grade(
        id = this[Grades.id].value,
        studentId = this[Grades.studentId].value,
        assignmentId = this[Grades.assignmentId].value,
        score = this[Grades.score],
        feedback = this[Grades.feedback],
        gradedAt = this[Grades.gradedAt]
    )
}