package com.example

import com.example.api.authRoutes
import com.example.api.plugins.configureKoin
import com.example.config.Config
import com.example.database.DatabaseProvider
import com.example.di.appModule
import com.example.services.auth.AuthService
import com.example.services.auth.TokenProvider
import com.example.util.ApiResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.module() {
    configureKoin()

    // Initialize database
    val databaseProvider = get<DatabaseProvider>()
    databaseProvider.init()

    // Configure plugins
    configureSerialization()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureSwagger()

    // Configure routing
    configureRouting()
}


fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        anyHost()
    }
}

fun Application.configureAuthentication() {
    val tokenProvider = get<TokenProvider>()

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(tokenProvider.getVerifier())
            realm = "smart-attendance-api"
            validate { credential ->
                val payload = credential.payload
                val id = payload.getClaim("id").asString()
                val email = payload.getClaim("email").asString()
                val role = payload.getClaim("role").asString()
                val type = payload.getClaim("type").asString()

                // Only accept access tokens for authentication
                if (id != null && email != null && role != null && type == "access") {
                    JWTPrincipal(payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("success" to false, "data" to null, "error" to "Invalid or expired token")
                )
            }
        }
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
    exception<Throwable> { call, cause ->

        val errorMessage = cause.message ?: "Something went wrong"
        val stackTrace = cause.stackTraceToString()

        //include stack trace for dev env
        val isDevelopment = System.getenv("ENVIRONMENT")?.lowercase() != "production"
        val detailedError = if (isDevelopment) "$errorMessage\n$stackTrace" else errorMessage

        call.respond(
            HttpStatusCode.InternalServerError,
            ApiResponse(
                success = false,
                message = "Something went wrong",
                data = null,
                error = detailedError,
            )
        )
    }
}
}

fun Application.configureSwagger() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}

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

        // Other route groups will be added here
        // e.g., userRoutes, courseRoutes, attendanceRoutes, etc.
    }
}