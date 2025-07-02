package com.example.features.users.repositories

import com.example.di.dbQuery
import com.example.domain.models.Student
import com.example.domain.tables.Students
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StudentRepository
 */
@Singleton
class StudentRepositoryImpl @Inject constructor() : StudentRepository {
    
    override suspend fun findByUserId(userId: UUID): Student? = dbQuery {
        Students.select { Students.userId eq userId }
            .map { it.toStudent() }
            .singleOrNull()
    }
    
    override suspend fun findByRegNo(regNo: String): Student? = dbQuery {
        Students.select { Students.regNo eq regNo }
            .map { it.toStudent() }
            .singleOrNull()
    }
    
    override suspend fun create(student: Student): Student = dbQuery {
        val insertStatement = Students.insert {
            it[userId] = student.userId
            it[regNo] = student.regNo
            it[department] = student.department
            it[yearOfStudy] = student.yearOfStudy
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toStudent()
            ?: throw Exception("Failed to insert student")
    }
    
    override suspend fun update(student: Student): Boolean = dbQuery {
        val updatedRows = Students.update({ Students.userId eq student.userId }) {
            it[regNo] = student.regNo
            it[department] = student.department
            it[yearOfStudy] = student.yearOfStudy
        }
        updatedRows > 0
    }
    
    override suspend fun delete(userId: UUID): Boolean = dbQuery {
        val deletedRows = Students.deleteWhere { Students.userId eq userId }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to Student model
     */
    private fun ResultRow.toStudent(): Student = Student(
        userId = this[Students.userId].value,
        regNo = this[Students.regNo],
        department = this[Students.department],
        yearOfStudy = this[Students.yearOfStudy]
    )
}
