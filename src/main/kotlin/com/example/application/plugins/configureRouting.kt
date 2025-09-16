package com.example.application.plugins

import com.example.api.authRoutes
import com.example.api.courseRoutes
import com.example.domain.services.auth.AuthService
import com.example.domain.services.courses.CourseService
import com.example.shared.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    val authService = get<AuthService>()
    val courseService = get<CourseService>()

    routing {
        // Root route
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "Welcome to Smart Attendance API",
                    data = null,
                    error = null
                )
            )
        }

        // Health check
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "Health check successful",
                    data = null,
                    error = null
                )
            )
        }

        // Authentication routes
        authRoutes(authService)

        courseRoutes(courseService)
        
    }
}