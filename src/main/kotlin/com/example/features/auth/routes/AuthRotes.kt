package com.example.features.auth.routes

import com.example.common.responses.success
import com.example.di.AppInjector
import com.example.features.auth.models.AuthResponse
import com.example.features.auth.models.LoginRequest
import com.example.features.auth.models.RefreshTokenRequest
import com.example.features.auth.models.SignUpRequest
import com.example.features.auth.models.SignUpRequestDTO
import com.example.features.auth.models.toSignUpRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Configure authentication routes
 */
fun Routing.configureAuthRoutes() {
    val authService = AppInjector.authService
    
    route("/auth") {
        // User registration endpoint
        post("/signup") {
            logger.info { "Received signup request" }
            
            val requestDTO = call.receive<SignUpRequestDTO>()

            val request = requestDTO.toSignUpRequest()
            
            val user = authService.signup(request)
            val tokens = authService.generateTokens(user)
            
            call.respond(
                HttpStatusCode.Created, 
                success(
                    AuthResponse(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        userId = user.id.toString(),
                        name = user.name,
                        email = user.email,
                        role = user.role.name
                    )
                )
            )
        }
        
        // User login endpoint
        post("/login") {
            logger.info { "Received login request" }
            
            val request = call.receive<LoginRequest>()
            
            val (user, tokens) = authService.login(request)
            
            call.respond(
                HttpStatusCode.OK,
                success(
                    AuthResponse(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        userId = user.id.toString(),
                        name = user.name,
                        email = user.email,
                        role = user.role.name
                    )
                )
            )
        }
        
        // Token refresh endpoint
        post("/refresh") {
            logger.info { "Received token refresh request" }
            
            val request = call.receive<RefreshTokenRequest>()
            
            val (user, tokens) = authService.refreshToken(request.refreshToken)
            
            call.respond(
                HttpStatusCode.OK,
                success(
                    AuthResponse(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        userId = user.id.toString(),
                        name = user.name,
                        email = user.email,
                        role = user.role.name
                    )
                )
            )
        }

        //reset password
    }
}