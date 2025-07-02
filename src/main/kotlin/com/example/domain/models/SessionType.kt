package com.example.domain.models

import java.time.Instant
import java.util.UUID

/**
 * Session types for attendance
 */
enum class SessionType {
    PHYSICAL, ONLINE
}

/**
 * Methods for attendance verification
 */
enum class VerificationMethod {
    MANUAL, QR_CODE, GEOLOCATION, BIOMETRIC, WEBCAM, OTP
}

/**
 * Attendance status values
 */
enum class AttendanceStatus {
    PRESENT, ABSENT, LATE
}

/**
 * Attendance session entity
 */
data class AttendanceSession(
    override val id: UUID,
    val courseId: UUID,
    val lecturerId: UUID,
    val sessionCode: String,
    val sessionType: SessionType,
    val createdAt: Instant,
    val expiresAt: Instant,
    val locationId: UUID? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Double? = null
) : BaseEntity

/**
 * Attendance record entity
 */
data class AttendanceRecord(
    override val id: UUID,
    val studentId: UUID,
    val courseId: UUID,
    val date: Instant,
    val status: AttendanceStatus,
    val sessionId: UUID? = null,
    val verificationMethod: VerificationMethod,
    val deviceId: String? = null,
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null
) : BaseEntity

/**
 * Attendance verification entity
 */
data class AttendanceVerification(
    override val id: UUID,
    val attendanceId: UUID,
    val verificationType: String,
    val verificationData: String? = null,  // Base64 encoded data
    val verifiedAt: Instant,
    val success: Boolean
) : BaseEntity

