package com.example.features.attendance.repositories

import com.example.di.dbQuery
import com.example.domain.models.AttendanceSession
import com.example.domain.models.SessionType
import com.example.domain.tables.AttendanceSessions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AttendanceSessionRepository
 */
@Singleton
class AttendanceSessionRepositoryImpl @Inject constructor() : AttendanceSessionRepository {
    
    override suspend fun getById(id: UUID): AttendanceSession? = dbQuery {
        AttendanceSessions.select { AttendanceSessions.id eq id }
            .map { it.toAttendanceSession() }
            .singleOrNull()
    }
    
    override suspend fun getBySessionCode(code: String): AttendanceSession? = dbQuery {
        AttendanceSessions.select { AttendanceSessions.sessionCode eq code }
            .map { it.toAttendanceSession() }
            .singleOrNull()
    }
    
    override suspend fun getActiveSessions(lecturerId: UUID): List<AttendanceSession> = dbQuery {
        val now = Instant.now()
        AttendanceSessions.select {
            (AttendanceSessions.lecturerId eq lecturerId) and
            (AttendanceSessions.expiresAt greater now)
        }
        .map { it.toAttendanceSession() }
    }
    
    override suspend fun getSessionsByCourse(courseId: UUID): List<AttendanceSession> = dbQuery {
        AttendanceSessions.select { AttendanceSessions.courseId eq courseId }
            .orderBy(AttendanceSessions.createdAt, SortOrder.DESC)
            .map { it.toAttendanceSession() }
    }
    
    override suspend fun create(session: AttendanceSession): AttendanceSession = dbQuery {
        val insertStatement = AttendanceSessions.insert {
            it[id] = session.id
            it[courseId] = session.courseId
            it[lecturerId] = session.lecturerId
            it[sessionCode] = session.sessionCode
            it[sessionType] = session.sessionType
            it[createdAt] = session.createdAt
            it[expiresAt] = session.expiresAt
            it[locationId] = session.locationId
            it[latitude] = session.latitude
            it[longitude] = session.longitude
            it[radiusMeters] = session.radiusMeters
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toAttendanceSession()
            ?: throw Exception("Failed to insert attendance session")
    }
    
    override suspend fun update(session: AttendanceSession): Boolean = dbQuery {
        val updatedRows = AttendanceSessions.update({ AttendanceSessions.id eq session.id }) {
            it[expiresAt] = session.expiresAt
            it[latitude] = session.latitude
            it[longitude] = session.longitude
            it[radiusMeters] = session.radiusMeters
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = AttendanceSessions.deleteWhere { AttendanceSessions.id eq id }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to AttendanceSession model
     */
    private fun ResultRow.toAttendanceSession(): AttendanceSession = AttendanceSession(
        id = this[AttendanceSessions.id].value,
        courseId = this[AttendanceSessions.courseId].value,
        lecturerId = this[AttendanceSessions.lecturerId].value,
        sessionCode = this[AttendanceSessions.sessionCode],
        sessionType = this[AttendanceSessions.sessionType],
        createdAt = this[AttendanceSessions.createdAt],
        expiresAt = this[AttendanceSessions.expiresAt],
        locationId = this[AttendanceSessions.locationId],
        latitude = this[AttendanceSessions.latitude],
        longitude = this[AttendanceSessions.longitude],
        radiusMeters = this[AttendanceSessions.radiusMeters]
    )
}
