package com.example.features.attendance.repositories

import com.example.domain.models.AttendanceRecord
import com.example.domain.models.AttendanceStatus
import java.time.Instant
import java.util.*

/**
 * Attendance record repository interface
 */
interface AttendanceRepository {
    suspend fun getById(id: UUID): AttendanceRecord?
    suspend fun getByStudentAndSession(studentId: UUID, sessionId: UUID): AttendanceRecord?
    suspend fun getByStudentAndCourse(studentId: UUID, courseId: UUID): List<AttendanceRecord>
    suspend fun getBySession(sessionId: UUID): List<AttendanceRecord>
    suspend fun getByCourse(courseId: UUID, fromDate: Instant? = null, toDate: Instant? = null): List<AttendanceRecord>
    suspend fun create(record: AttendanceRecord): AttendanceRecord
    suspend fun updateStatus(id: UUID, status: AttendanceStatus): Boolean
    suspend fun delete(id: UUID): Boolean
}