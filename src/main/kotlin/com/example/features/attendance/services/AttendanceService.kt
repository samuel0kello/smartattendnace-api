package com.example.features.attendance.services

import com.example.common.exceptions.BadRequestException
import com.example.common.exceptions.ConflictException
import com.example.common.exceptions.ForbiddenException
import com.example.common.exceptions.NotFoundException
import com.example.domain.models.*
import com.example.features.attendance.models.AttendanceResponseDto
import com.example.features.attendance.models.CreateSessionRequest
import com.example.features.attendance.models.MarkAttendanceRequest
import com.example.features.attendance.models.SessionResponseDto
import com.example.features.attendance.repositories.AttendanceRepository
import com.example.features.attendance.repositories.AttendanceSessionRepository
import com.example.features.courses.repositories.CourseRepository
import com.example.features.users.repositories.UserRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val logger = KotlinLogging.logger {}

/**
 * Attendance service interface
 */
interface AttendanceService {
    suspend fun createSession(lecturerId: UUID, request: CreateSessionRequest): SessionResponseDto
    suspend fun getSessionById(id: UUID): SessionResponseDto
    suspend fun getSessionByCode(code: String): SessionResponseDto
    suspend fun getActiveSessionsForLecturer(lecturerId: UUID): List<SessionResponseDto>
    suspend fun getSessionsForCourse(courseId: UUID): List<SessionResponseDto>
    suspend fun generateQRCode(sessionCode: String, width: Int = 300, height: Int = 300): ByteArray
    suspend fun markAttendance(studentId: UUID, request: MarkAttendanceRequest): AttendanceResponseDto
    suspend fun getAttendanceForStudent(studentId: UUID, courseId: UUID): List<AttendanceResponseDto>
    suspend fun getAttendanceForSession(sessionId: UUID): List<AttendanceResponseDto>
    suspend fun getAttendanceForCourse(courseId: UUID, fromDate: Instant? = null, toDate: Instant? = null): List<AttendanceResponseDto>
    suspend fun updateAttendanceStatus(id: UUID, status: AttendanceStatus): AttendanceResponseDto
}

/**
 * Implementation of AttendanceService
 */
@Singleton
class AttendanceServiceImpl @Inject constructor(
    private val attendanceSessionRepository: AttendanceSessionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) : AttendanceService {

    /**
     * Create a new attendance session
     */
    override suspend fun createSession(lecturerId: UUID, request: CreateSessionRequest): SessionResponseDto {
        logger.info { "Creating attendance session for lecturer $lecturerId" }

        // Validate request
        if (request.courseId.isBlank()) {
            throw BadRequestException("Course ID is required")
        }

        if (request.durationMinutes <= 0) {
            throw BadRequestException("Duration must be greater than 0 minutes")
        }

        // Parse session type
        val sessionType = try {
            SessionType.valueOf(request.sessionType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid session type: ${request.sessionType}")
        }

        // Validate geofence for physical sessions
        if (sessionType == SessionType.PHYSICAL) {
            if (request.geoFence == null) {
                throw BadRequestException("Geofence is required for physical sessions")
            }

            if (request.geoFence.latitude < -90 || request.geoFence.latitude > 90) {
                throw BadRequestException("Latitude must be between -90 and 90")
            }

            if (request.geoFence.longitude < -180 || request.geoFence.longitude > 180) {
                throw BadRequestException("Longitude must be between -180 and 180")
            }

            if (request.geoFence.radiusMeters <= 0) {
                throw BadRequestException("Radius must be greater than 0 meters")
            }
        }

        // Verify course and lecturer exist
        val courseId = UUID.fromString(request.courseId)
        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        // Check if lecturer owns the course
        if (course.lecturerId != lecturerId) {
            throw ForbiddenException("You don't have permission to create sessions for this course")
        }

        // Generate session details
        val sessionCode = generateSessionCode()
        val now = Instant.now()
        val expiresAt = now.plus(request.durationMinutes.toLong(), ChronoUnit.MINUTES)

        val session = AttendanceSession(
            id = UUID.randomUUID(),
            courseId = courseId,
            lecturerId = lecturerId,
            sessionCode = sessionCode,
            sessionType = sessionType,
            createdAt = now,
            expiresAt = expiresAt,
            locationId = null,
            latitude = request.geoFence?.latitude,
            longitude = request.geoFence?.longitude,
            radiusMeters = request.geoFence?.radiusMeters
        )

        val createdSession = attendanceSessionRepository.create(session)
        logger.info { "Created attendance session with ID: ${createdSession.id}, code: $sessionCode" }

        return SessionResponseDto.fromSession(
            session = createdSession,
            courseName = course.name,
            lecturerName = lecturer.name
        )
    }

    /**
     * Get an attendance session by ID
     */
    override suspend fun getSessionById(id: UUID): SessionResponseDto {
        val session = attendanceSessionRepository.getById(id)
            ?: throw NotFoundException("Attendance session not found")

        val course = courseRepository.getById(session.courseId)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(session.lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        return SessionResponseDto.fromSession(
            session = session,
            courseName = course.name,
            lecturerName = lecturer.name
        )
    }

    /**
     * Get an attendance session by its code
     */
    override suspend fun getSessionByCode(code: String): SessionResponseDto {
        val session = attendanceSessionRepository.getBySessionCode(code)
            ?: throw NotFoundException("Attendance session not found")

        val course = courseRepository.getById(session.courseId)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(session.lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        return SessionResponseDto.fromSession(
            session = session,
            courseName = course.name,
            lecturerName = lecturer.name
        )
    }

    /**
     * Get active sessions for a lecturer
     */
    override suspend fun getActiveSessionsForLecturer(lecturerId: UUID): List<SessionResponseDto> {
        val sessions = attendanceSessionRepository.getActiveSessions(lecturerId)

        return sessions.mapNotNull { session ->
            val course = courseRepository.getById(session.courseId) ?: return@mapNotNull null
            val lecturer = userRepository.getById(session.lecturerId) ?: return@mapNotNull null

            SessionResponseDto.fromSession(
                session = session,
                courseName = course.name,
                lecturerName = lecturer.name
            )
        }
    }

    /**
     * Get sessions for a course
     */
    override suspend fun getSessionsForCourse(courseId: UUID): List<SessionResponseDto> {
        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        val sessions = attendanceSessionRepository.getSessionsByCourse(courseId)

        return sessions.mapNotNull { session ->
            val lecturer = userRepository.getById(session.lecturerId) ?: return@mapNotNull null

            SessionResponseDto.fromSession(
                session = session,
                courseName = course.name,
                lecturerName = lecturer.name
            )
        }
    }

    /**
     * Generate a QR code for the attendance session
     */
    override suspend fun generateQRCode(sessionCode: String, width: Int, height: Int): ByteArray {
        logger.debug { "Generating QR code for session code: $sessionCode" }

        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            sessionCode,
            BarcodeFormat.QR_CODE,
            width,
            height
        )

        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)

        return outputStream.toByteArray()
    }

    /**
     * Mark student attendance
     */
    override suspend fun markAttendance(studentId: UUID, request: MarkAttendanceRequest): AttendanceResponseDto {
        logger.info { "Marking attendance for student $studentId with session code: ${request.sessionCode}" }

        // Get session by code
        val session = attendanceSessionRepository.getBySessionCode(request.sessionCode)
            ?: throw NotFoundException("Invalid session code")

        // Check if session is still active
        val now = Instant.now()
        if (now.isAfter(session.expiresAt)) {
            throw BadRequestException("Attendance session has expired")
        }

        // Check if student already marked attendance for this session
        if (session.sessionCode != null &&
            attendanceRepository.getByStudentAndSession(studentId, session.id) != null) {
            throw ConflictException("Attendance already marked for this session")
        }

        // Verify student exists
        val student = userRepository.getById(studentId)
            ?: throw NotFoundException("Student not found")

        if (student.role != UserRole.STUDENT) {
            throw BadRequestException("Only students can mark attendance")
        }

        // Verify course exists
        val course = courseRepository.getById(session.courseId)
            ?: throw NotFoundException("Course not found")

        // For physical sessions, verify location if provided
        if (session.sessionType == SessionType.PHYSICAL &&
            session.latitude != null && session.longitude != null && session.radiusMeters != null) {

            if (request.location == null) {
                throw BadRequestException("Location is required for physical attendance")
            }

            val isWithinRadius = checkIfWithinRadius(
                request.location.latitude,
                request.location.longitude,
                session.latitude,
                session.longitude,
                session.radiusMeters
            )

            if (!isWithinRadius) {
                throw BadRequestException("You are not within the required attendance area")
            }
        }

        // Create attendance record
        val attendanceRecord = AttendanceRecord(
            id = UUID.randomUUID(),
            studentId = studentId,
            courseId = session.courseId,
            date = now,
            status = AttendanceStatus.PRESENT, // Default to present
            sessionId = session.id,
            verificationMethod = VerificationMethod.QR_CODE, // Default to QR code
            deviceId = request.deviceId,
            locationLatitude = request.location?.latitude,
            locationLongitude = request.location?.longitude
        )

        val createdRecord = attendanceRepository.create(attendanceRecord)
        logger.info { "Created attendance record with ID: ${createdRecord.id}" }

        return AttendanceResponseDto.fromAttendanceRecord(
            record = createdRecord,
            studentName = student.name,
            courseName = course.name
        )
    }

    /**
     * Get attendance records for a student in a course
     */
    override suspend fun getAttendanceForStudent(studentId: UUID, courseId: UUID): List<AttendanceResponseDto> {
        val student = userRepository.getById(studentId)
            ?: throw NotFoundException("Student not found")

        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        val records = attendanceRepository.getByStudentAndCourse(studentId, courseId)

        return records.map { record ->
            AttendanceResponseDto.fromAttendanceRecord(
                record = record,
                studentName = student.name,
                courseName = course.name
            )
        }
    }

    /**
     * Get attendance records for a session
     */
    override suspend fun getAttendanceForSession(sessionId: UUID): List<AttendanceResponseDto> {
        val session = attendanceSessionRepository.getById(sessionId)
            ?: throw NotFoundException("Attendance session not found")

        val course = courseRepository.getById(session.courseId)
            ?: throw NotFoundException("Course not found")

        val records = attendanceRepository.getBySession(sessionId)

        return records.mapNotNull { record ->
            val student = userRepository.getById(record.studentId) ?: return@mapNotNull null

            AttendanceResponseDto.fromAttendanceRecord(
                record = record,
                studentName = student.name,
                courseName = course.name
            )
        }
    }

    /**
     * Get attendance records for a course
     */
    override suspend fun getAttendanceForCourse(courseId: UUID, fromDate: Instant?, toDate: Instant?): List<AttendanceResponseDto> {
        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        val records = attendanceRepository.getByCourse(courseId, fromDate, toDate)

        return records.mapNotNull { record ->
            val student = userRepository.getById(record.studentId) ?: return@mapNotNull null

            AttendanceResponseDto.fromAttendanceRecord(
                record = record,
                studentName = student.name,
                courseName = course.name
            )
        }
    }

    /**
     * Update an attendance record's status
     */
    override suspend fun updateAttendanceStatus(id: UUID, status: AttendanceStatus): AttendanceResponseDto {
        val record = attendanceRepository.getById(id)
            ?: throw NotFoundException("Attendance record not found")

        if (!attendanceRepository.updateStatus(id, status)) {
            throw Exception("Failed to update attendance status")
        }

        val updatedRecord = record.copy(status = status)

        val student = userRepository.getById(record.studentId)
            ?: throw NotFoundException("Student not found")

        val course = courseRepository.getById(record.courseId)
            ?: throw NotFoundException("Course not found")

        return AttendanceResponseDto.fromAttendanceRecord(
            record = updatedRecord,
            studentName = student.name,
            courseName = course.name
        )
    }

    /**
     * Generate a random session code
     */
    private fun generateSessionCode(length: Int = 6): String {
        val charPool = ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }

    /**
     * Check if coordinates are within a radius
     */
    private fun checkIfWithinRadius(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        radiusMeters: Double
    ): Boolean {
        val earthRadius = 6371000.0 // Earth radius in meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = earthRadius * c
        return distance <= radiusMeters
    }
}