package com.example.features.courses.repositories

import com.example.di.dbQuery
import com.example.domain.models.Course
import com.example.domain.tables.Courses
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CourseRepository
 */
@Singleton
class CourseRepositoryImpl @Inject constructor() : CourseRepository {
    
    override suspend fun getById(id: UUID): Course? = dbQuery {
        Courses.select { Courses.id eq id }
            .map { it.toCourse() }
            .singleOrNull()
    }
    
    override suspend fun getAll(): List<Course> = dbQuery {
        Courses.selectAll()
            .map { it.toCourse() }
    }
    
    override suspend fun create(entity: Course): Course = dbQuery {
        val insertStatement = Courses.insert {
            it[id] = entity.id
            it[name] = entity.name
            it[lecturerId] = entity.lecturerId
            it[createdAt] = entity.createdAt
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toCourse()
            ?: throw Exception("Failed to insert course")
    }
    
    override suspend fun update(entity: Course): Boolean = dbQuery {
        val updatedRows = Courses.update({ Courses.id eq entity.id }) {
            it[name] = entity.name
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = Courses.deleteWhere { Courses.id eq id }
        deletedRows > 0
    }
    
    override suspend fun getCoursesByLecturerId(lecturerId: UUID): List<Course> = dbQuery {
        Courses.select { Courses.lecturerId eq lecturerId }
            .map { it.toCourse() }
    }
    
    override suspend fun getCoursesForStudent(studentId: UUID): List<Course> = dbQuery {
        // This would join with an enrollment table in a real implementation
        // For now, we'll just return an empty list
        emptyList()
    }
    
    /**
     * Convert ResultRow to Course model
     */
    private fun ResultRow.toCourse(): Course = Course(
        id = this[Courses.id].value,
        name = this[Courses.name],
        lecturerId = this[Courses.lecturerId].value,
        createdAt = this[Courses.createdAt]
    )
}
