package com.example.features.attendance.models
import com.example.domain.models.AttendanceRecord
import com.example.domain.models.AttendanceSession
import kotlinx.serialization.Serializable

/**
 * Attendance session request DTO
 */
@Serializable
data class CreateSessionRequest(
    val courseId: String,
    val sessionType: String,
    val durationMinutes: Int,
    val geoFence: GeoFenceDto? = null
)

/**
 * Geofencing data for physical attendance
 */
@Serializable
data class GeoFenceDto(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double
)

/**
 * Attendance session response DTO
 */
@Serializable
data class SessionResponseDto(
    val id: String,
    val courseId: String,
    val courseName: String,
    val lecturerId: String,
    val lecturerName: String,
    val sessionCode: String,
    val sessionType: String,
    val createdAt: String,
    val expiresAt: String,
    val location: LocationDto? = null
) {
    companion object {
        fun fromSession(
            session: AttendanceSession,
            courseName: String,
            lecturerName: String
        ): SessionResponseDto {
            val location = if (session.latitude != null && session.longitude != null) {
                LocationDto(
                    latitude = session.latitude,
                    longitude = session.longitude,
                    radiusMeters = session.radiusMeters
                )
            } else null

            return SessionResponseDto(
                id = session.id.toString(),
                courseId = session.courseId.toString(),
                courseName = courseName,
                lecturerId = session.lecturerId.toString(),
                lecturerName = lecturerName,
                sessionCode = session.sessionCode,
                sessionType = session.sessionType.name,
                createdAt = session.createdAt.toString(),
                expiresAt = session.expiresAt.toString(),
                location = location
            )
        }
    }
}

/**
 * Location data DTO
 */
@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double? = null
)

/**
 * Mark attendance request DTO
 */
@Serializable
data class MarkAttendanceRequest(
    val sessionCode: String,
    val location: LocationDto? = null,
    val deviceId: String? = null
)

/**
 * Attendance record response DTO
 */
@Serializable
data class AttendanceResponseDto(
    val id: String,
    val studentId: String,
    val studentName: String,
    val courseId: String,
    val courseName: String,
    val sessionId: String? = null,
    val date: String,
    val status: String,
    val verificationMethod: String
) {
    companion object {
        fun fromAttendanceRecord(
            record: AttendanceRecord,
            studentName: String,
            courseName: String
        ): AttendanceResponseDto {
            return AttendanceResponseDto(
                id = record.id.toString(),
                studentId = record.studentId.toString(),
                studentName = studentName,
                courseId = record.courseId.toString(),
                courseName = courseName,
                sessionId = record.sessionId?.toString(),
                date = record.date.toString(),
                status = record.status.name,
                verificationMethod = record.verificationMethod.name
            )
        }
    }
}