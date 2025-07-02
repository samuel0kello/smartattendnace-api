package com.example.features.users.repositories

import com.example.di.dbQuery
import com.example.domain.models.Staff
import com.example.domain.tables.Staff as StaffTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StaffRepository
 */
@Singleton
class StaffRepositoryImpl @Inject constructor() : StaffRepository {
    
    override suspend fun findByUserId(userId: UUID): Staff? = dbQuery {
        StaffTable.select { StaffTable.userId eq userId }
            .map { it.toStaff() }
            .singleOrNull()
    }
    
    override suspend fun findByEmployeeId(employeeId: String): Staff? = dbQuery {
        StaffTable.select { StaffTable.employeeId eq employeeId }
            .map { it.toStaff() }
            .singleOrNull()
    }
    
    override suspend fun create(staff: Staff): Staff = dbQuery {
        val insertStatement = StaffTable.insert {
            it[userId] = staff.userId
            it[employeeId] = staff.employeeId
            it[department] = staff.department
            it[position] = staff.position
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toStaff()
            ?: throw Exception("Failed to insert staff")
    }
    
    override suspend fun update(staff: Staff): Boolean = dbQuery {
        val updatedRows = StaffTable.update({ StaffTable.userId eq staff.userId }) {
            it[employeeId] = staff.employeeId
            it[department] = staff.department
            it[position] = staff.position
        }
        updatedRows > 0
    }
    
    override suspend fun delete(userId: UUID): Boolean = dbQuery {
        val deletedRows = StaffTable.deleteWhere { StaffTable.userId eq userId }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to Staff model
     */
    private fun ResultRow.toStaff(): Staff = Staff(
        userId = this[StaffTable.userId].value,
        employeeId = this[StaffTable.employeeId],
        department = this[StaffTable.department],
        position = this[StaffTable.position]
    )
}
