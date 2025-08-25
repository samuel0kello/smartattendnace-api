package com.example.plugins

import com.example.services.auth.TokenProvider
import com.example.util.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.koin.ktor.ext.get

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
                    ApiResponse(
                        success = false,
                        message = "Token is invalid or expired",
                        data = null,
                        error =  "Invalid or expired token"
                    )
                )
            }
        }
    }
}