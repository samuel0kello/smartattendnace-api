package com.example.api

import com.example.model.ChangePasswordRequest
import com.example.model.LoginCredentials
import com.example.model.PasswordResetRequest
import com.example.model.RefreshTokenRequest
import com.example.model.UserRegistrationRequest
import com.example.services.auth.AuthService
import com.example.util.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.lang.IllegalArgumentException
import java.util.UUID

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post ("/sign-up"){
            try {
                val request = call.receive<UserRegistrationRequest>()
                val user = authService.registerUser(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(success = true, message = "User registered successfully", data = user)
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            }
        }

        post("/sign-in") {
            try {
                val credentials = call.receive<LoginCredentials>()
                val token = authService.login(credentials)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "User logged in successfully", data = token)
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            }
        }

        post("/refresh") {
            try {
                val request = call.receive<RefreshTokenRequest>()
                val token = authService.refreshToken(request)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "Token refreshed successfully", data = token)
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            }
        }

        post("/forgot-password") {
            try {
                val request = call.receive<PasswordResetRequest>()
                val response = authService.resetPassword(request.email)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "password reset email sent", data = response)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message)
                )
            }
        }

        authenticate("auth-jwt") {
            post("/change-password") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim("id", String::class)
                        ?: throw IllegalArgumentException("Invalid user ID")

                    val request = call.receive<ChangePasswordRequest>()
                    val user = authService.changePassword(UUID.fromString(userId), request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "Password changed successfully", data = user)
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = e.message, data = null, error = e.message)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse(success = false, message = e.message, data = null, error = e.message)
                    )
                }
            }

            post("/verify-email") {

            }
        }
    }
}