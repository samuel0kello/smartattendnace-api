package com.example.plugins

fun Application.configureRouting() {
    val authService = get<AuthService>()

    routing {
        // Root route
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "success" to true,
                    "data" to mapOf(
                        "message" to "Smart Attendance API is running",
                        "version" to "1.0.0",
                        "docs" to "/swagger"
                    ),
                    "error" to null
                )
            )
        }

        // Health check
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "success" to true,
                    "data" to mapOf(
                        "status" to "UP",
                        "timestamp" to System.currentTimeMillis()
                    ),
                    "error" to null
                )
            )
        }

        // Authentication routes
        authRoutes(authService)
        
    }
}