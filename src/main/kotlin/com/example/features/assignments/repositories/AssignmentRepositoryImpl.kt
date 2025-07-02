package com.example.features.assignments.repositories

import com.example.di.dbQuery
import com.example.domain.models.Assignment
import com.example.domain.tables.Assignments
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AssignmentRepository
 */
@Singleton
class AssignmentRepositoryImpl @Inject constructor() : AssignmentRepository {
    
    override suspend fun getById(id: UUID): Assignment? = dbQuery {
        Assignments.select { Assignments.id eq id }
            .map { it.toAssignment() }
            .singleOrNull()
    }
    
    override suspend fun getAll(): List<Assignment> = dbQuery {
        Assignments.selectAll()
            .map { it.toAssignment() }
    }
    
    override suspend fun create(entity: Assignment): Assignment = dbQuery {
        val insertStatement = Assignments.insert {
            it[id] = entity.id
            it[courseId] = entity.courseId
            it[lecturerId] = entity.lecturerId
            it[title] = entity.title
            it[description] = entity.description
            it[dueDate] = entity.dueDate
            it[createdAt] = entity.createdAt
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toAssignment()
            ?: throw Exception("Failed to insert assignment")
    }
    
    override suspend fun update(entity: Assignment): Boolean = dbQuery {
        val updatedRows = Assignments.update({ Assignments.id eq entity.id }) {
            it[title] = entity.title
            it[description] = entity.description
            it[dueDate] = entity.dueDate
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = Assignments.deleteWhere { Assignments.id eq id }
        deletedRows > 0
    }
    
    override suspend fun getAssignmentsByCourse(courseId: UUID): List<Assignment> = dbQuery {
        Assignments.select { Assignments.courseId eq courseId }
            .orderBy(Assignments.dueDate, SortOrder.DESC)
            .map { it.toAssignment() }
    }
    
    override suspend fun getAssignmentsByLecturer(lecturerId: UUID): List<Assignment> = dbQuery {
        Assignments.select { Assignments.lecturerId eq lecturerId }
            .orderBy(Assignments.dueDate, SortOrder.DESC)
            .map { it.toAssignment() }
    }
    
    /**
     * Convert ResultRow to Assignment model
     */
    private fun ResultRow.toAssignment(): Assignment = Assignment(
        id = this[Assignments.id].value,
        courseId = this[Assignments.courseId].value,
        lecturerId = this[Assignments.lecturerId].value,
        title = this[Assignments.title],
        description = this[Assignments.description],
        dueDate = this[Assignments.dueDate],
        createdAt = this[Assignments.createdAt]
    )
}
