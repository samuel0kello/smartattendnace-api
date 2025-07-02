package com.example.features.attendance.repositories

import com.example.di.dbQuery
import com.example.domain.models.AttendanceRecord
import com.example.domain.models.AttendanceStatus
import com.example.domain.models.VerificationMethod
import com.example.domain.tables.Attendance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AttendanceRepository
 */
@Singleton
class AttendanceRepositoryImpl @Inject constructor() : AttendanceRepository {
    
    override suspend fun getById(id: UUID): AttendanceRecord? = dbQuery {
        Attendance.select { Attendance.id eq id }
            .map { it.toAttendanceRecord() }
            .singleOrNull()
    }
    
    override suspend fun getByStudentAndSession(studentId: UUID, sessionId: UUID): AttendanceRecord? = dbQuery {
        Attendance.select {
            (Attendance.studentId eq studentId) and (Attendance.sessionId eq sessionId)
        }
        .map { it.toAttendanceRecord() }
        .singleOrNull()
    }
    
    override suspend fun getByStudentAndCourse(studentId: UUID, courseId: UUID): List<AttendanceRecord> = dbQuery {
        Attendance.select {
            (Attendance.studentId eq studentId) and (Attendance.courseId eq courseId)
        }
        .orderBy(Attendance.date, SortOrder.DESC)
        .map { it.toAttendanceRecord() }
    }
    
    override suspend fun getBySession(sessionId: UUID): List<AttendanceRecord> = dbQuery {
        Attendance.select { Attendance.sessionId eq sessionId }
            .orderBy(Attendance.date, SortOrder.DESC)
            .map { it.toAttendanceRecord() }
    }
    
    override suspend fun getByCourse(courseId: UUID, fromDate: Instant?, toDate: Instant?): List<AttendanceRecord> = dbQuery {
        var query = Attendance.select { Attendance.courseId eq courseId }
        
        if (fromDate != null) {
            query = query.andWhere { Attendance.date greaterEq fromDate }
        }
        
        if (toDate != null) {
            query = query.andWhere { Attendance.date lessEq toDate }
        }
        
        query.orderBy(Attendance.date, SortOrder.DESC)
            .map { it.toAttendanceRecord() }
    }
    
    override suspend fun create(record: AttendanceRecord): AttendanceRecord = dbQuery {
        val insertStatement = Attendance.insert {
            it[id] = record.id
            it[studentId] = record.studentId
            it[courseId] = record.courseId
            it[date] = record.date
            it[status] = record.status
            it[sessionId] = record.sessionId
            it[verificationMethod] = record.verificationMethod
            it[deviceId] = record.deviceId
            it[locationLatitude] = record.locationLatitude
            it[locationLongitude] = record.locationLongitude
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toAttendanceRecord()
            ?: throw Exception("Failed to insert attendance record")
    }
    
    override suspend fun updateStatus(id: UUID, status: AttendanceStatus): Boolean = dbQuery {
        val updatedRows = Attendance.update({ Attendance.id eq id }) {
            it[Attendance.status] = status
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = Attendance.deleteWhere { Attendance.id eq id }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to AttendanceRecord model
     */
    private fun ResultRow.toAttendanceRecord(): AttendanceRecord = AttendanceRecord(
        id = this[Attendance.id].value,
        studentId = this[Attendance.studentId].value,
        courseId = this[Attendance.courseId].value,
        date = this[Attendance.date],
        status = this[Attendance.status],
        sessionId = this[Attendance.sessionId]?.value,
        verificationMethod = this[Attendance.verificationMethod],
        deviceId = this[Attendance.deviceId],
        locationLatitude = this[Attendance.locationLatitude],
        locationLongitude = this[Attendance.locationLongitude]
    )
}