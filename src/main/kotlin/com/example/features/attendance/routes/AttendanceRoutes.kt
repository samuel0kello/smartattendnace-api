package com.example.features.attendance.routes

import com.example.common.responses.success
import com.example.di.AppInjector
import com.example.domain.models.AttendanceStatus
import com.example.features.attendance.models.CreateSessionRequest
import com.example.features.attendance.models.MarkAttendanceRequest
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Configure attendance routes
 */
fun Routing.configureAttendanceRoutes() {
    val attendanceService = AppInjector.attendanceService
    val roleAuthorization = AppInjector.roleAuthorization

    authenticate("auth-jwt") {
        route("/attendance") {
            // Create a new attendance session (Lecturer only)
            post("/sessions") {
                val userId = roleAuthorization.getUserId(call)
                roleAuthorization.requireLecturer(call)

                val request = call.receive<CreateSessionRequest>()
                val session = attendanceService.createSession(UUID.fromString(userId), request)

                call.respond(HttpStatusCode.Created, success(session))
            }

            // Get attendance session by ID
            get("/sessions/{id}") {
                val id = call.parameters["id"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid session ID format")

                val session = attendanceService.getSessionById(id)
                call.respond(success(session))
            }

            // Get session by code
            get("/sessions/code/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Session code is required")

                val session = attendanceService.getSessionByCode(code)
                call.respond(success(session))
            }

            // Generate QR code for a session
            get("/sessions/{id}/qr") {
                val id = call.parameters["id"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid session ID format")

                val session = attendanceService.getSessionById(id)
                val qrCodeImage = attendanceService.generateQRCode(session.sessionCode)

                call.respond(ByteArrayContent(qrCodeImage, ContentType.Image.PNG))
            }

            // Get active sessions for the current lecturer
            get("/sessions/lecturer/active") {
                val userId = roleAuthorization.getUserId(call)
                roleAuthorization.requireLecturer(call)

                val sessions = attendanceService.getActiveSessionsForLecturer(UUID.fromString(userId))
                call.respond(success(sessions))
            }

            // Get all sessions for a course
            get("/sessions/course/{courseId}") {
                val courseId = call.parameters["courseId"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid course ID format")

                val sessions = attendanceService.getSessionsForCourse(courseId)
                call.respond(success(sessions))
            }

            // Mark attendance (Student only)
            post("/mark") {
                val userId = roleAuthorization.getUserId(call)
                roleAuthorization.requireStudent(call)

                val request = call.receive<MarkAttendanceRequest>()
                val attendanceRecord = attendanceService.markAttendance(UUID.fromString(userId), request)

                call.respond(HttpStatusCode.Created, success(attendanceRecord))
            }

            // Get attendance records for current student in a course
            get("/student/course/{courseId}") {
                val userId = roleAuthorization.getUserId(call)
                roleAuthorization.requireStudent(call)

                val courseId = call.parameters["courseId"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid course ID format")

                val records = attendanceService.getAttendanceForStudent(UUID.fromString(userId), courseId)
                call.respond(success(records))
            }

            // Get attendance records for a specific student in a course (Lecturer/Admin only)
            get("/student/{studentId}/course/{courseId}") {
                roleAuthorization.requireAdminOrLecturer(call)

                val studentId = call.parameters["studentId"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid student ID format")

                val courseId = call.parameters["courseId"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid course ID format")

                val records = attendanceService.getAttendanceForStudent(studentId, courseId)
                call.respond(success(records))
            }

            // Get attendance records for a session (Lecturer/Admin only)
            get("/session/{sessionId}") {
                roleAuthorization.requireAdminOrLecturer(call)

                val sessionId = call.parameters["sessionId"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid session ID format")

                val records = attendanceService.getAttendanceForSession(sessionId)
                call.respond(success(records))
            }

            // Get attendance records for a course (Lecturer/Admin only)
            get("/course/{courseId}") {
                roleAuthorization.requireAdminOrLecturer(call)

                val courseId = call.parameters["courseId"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid course ID format")

                val fromDateStr = call.request.queryParameters["fromDate"]
                val toDateStr = call.request.queryParameters["toDate"]

                val fromDate = fromDateStr?.let { Instant.parse(it) }
                val toDate = toDateStr?.let { Instant.parse(it) }

                val records = attendanceService.getAttendanceForCourse(courseId, fromDate, toDate)
                call.respond(success(records))
            }

            // Update attendance status (Lecturer/Admin only)
            put("/{id}/status/{status}") {
                roleAuthorization.requireAdminOrLecturer(call)

                val id = call.parameters["id"]?.let {
                    try {
                        UUID.fromString(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: throw IllegalArgumentException("Invalid attendance record ID format")

                val statusStr = call.parameters["status"]?.uppercase() ?: throw IllegalArgumentException("Status is required")
                val status = try {
                    AttendanceStatus.valueOf(statusStr)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid status: $statusStr")
                }

                val updatedRecord = attendanceService.updateAttendanceStatus(id, status)
                call.respond(success(updatedRecord))
            }
        }
    }
}