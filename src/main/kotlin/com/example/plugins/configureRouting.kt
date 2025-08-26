package com.example.plugins

import com.example.api.authRoutes
import com.example.services.auth.AuthService
import com.example.util.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    val authService = get<AuthService>()

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
        
    }
}