package com.example.plugins

import com.example.features.attendance.routes.configureAttendanceRoutes
import com.example.features.auth.routes.configureAuthRoutes
import com.example.features.courses.routes.configureCourseRoutes
import com.example.features.users.routes.configureUserRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Configure routing
 */
fun Application.configureRouting() {
    logger.info { "Configuring routing..." }

    routing {
        // API version and health check
        get("/") {
            call.respond(mapOf(
                "name" to "Smart Attendance API",
                "version" to "1.0.0",
                "status" to "running"
            ))
        }
        configureAuthRoutes()
        configureUserRoutes()
        configureCourseRoutes()
        configureAttendanceRoutes()
    }

    logger.info { "Routing configuration complete" }
}