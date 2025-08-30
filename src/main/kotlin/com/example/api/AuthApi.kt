package com.example.api

import com.example.model.ChangePasswordRequest
import com.example.model.LoginCredentials
import com.example.model.PasswordResetRequest
import com.example.model.RefreshTokenRequest
import com.example.model.UserRegistrationRequest
import com.example.services.auth.AuthService
import com.example.util.ApiResponse
import com.example.web.RequestUtils
import com.example.web.Routes
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.lang.IllegalArgumentException
import java.util.UUID

fun Route.authRoutes(authService: AuthService) {
    post(Routes.SIGN_UP) {
        try {
            val request = call.receive<UserRegistrationRequest>()
            val baseUrl = RequestUtils.requestBaseUrl(call)
            val userResponse = authService.registerUser(request, baseUrl)
            call.respond(
                HttpStatusCode.Created,
                ApiResponse(success = true, message = "User registered successfully", data = userResponse),
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        }
    }

    post(Routes.SIGN_IN) {
        try {
            val credentials = call.receive<LoginCredentials>()
            val token = authService.login(credentials)
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(success = true, message = "User logged in successfully", data = token),
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        }
    }

    post(Routes.REFRESH) {
        try {
            val request = call.receive<RefreshTokenRequest>()
            val token = authService.refreshToken(request)
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(success = true, message = "Token refreshed successfully", data = token),
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        }
    }

    post(Routes.FORGOT_PASSWORD) {
        try {
            val request = call.receive<PasswordResetRequest>()
            val response = authService.resetPassword(request.email)
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(success = true, message = "password reset email sent", data = response),
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(success = false, message = e.message, data = null, error = e.message),
            )
        }
    }

    get(Routes.VERIFY_EMAIL) {
        try {
            val token =
                call.parameters["token"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Token is required")

            val verified = authService.verifyEmail(token)
            if (verified) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Email verified successfully"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Email verification failed"))
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to e.message))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "An error occurred"))
        }
    }

    authenticate("auth-jwt") {
        post(Routes.CHANGE_PASSWORD) {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId =
                    principal?.getClaim("id", String::class)
                        ?: throw IllegalArgumentException("Invalid user ID")

                val request = call.receive<ChangePasswordRequest>()
                val user = authService.changePassword(UUID.fromString(userId), request)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "Password changed successfully", data = user),
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message),
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(success = false, message = e.message, data = null, error = e.message),
                )
            }
        }
    }
}
