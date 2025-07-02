package com.example.plugins

import com.example.config.AppConfig
import com.example.di.AppInjector
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Configure JWT authentication
 */
fun Application.configureAuthentication() {
    logger.info { "Configuring authentication..." }

    val jwtService = AppInjector.jwtService
    val config = AppConfig.jwt

    install(Authentication) {
        jwt("auth-jwt") {
            realm = config.realm
            verifier(jwtService.getJWTVerifier())

            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrEmpty()) {
                    JWTPrincipal(credential.payload)
                } else null
            }

            challenge { _, _ ->
                logger.warn { "Authentication failure: Invalid or missing JWT token" }
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid token")
                )
            }
        }
    }
}