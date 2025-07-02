package com.example.common.util

import com.example.config.AppConfig
import io.ktor.server.application.*

fun Application.loadJwtConfig(): AppConfig.JwtConfig {
    val isDevelopment = environment.config.propertyOrNull("ktor.deployment.development")?.getString()?.toBoolean() ?: true

    val secret = System.getenv("JWT_SECRET")
        ?: if (isDevelopment) {
            log.warn("JWT_SECRET not set. Using development-only default. DO NOT USE IN PRODUCTION!")
            "development-secret-do-not-use-in-production"
        } else {
            throw IllegalStateException("JWT_SECRET must be set in production mode")
        }

    val issuer = System.getenv("JWT_ISSUER") ?: error("JWT_ISSUER must be set in production mode")
    val audience = System.getenv("JWT_AUDIENCE") ?: error("JWT_AUDIENCE must be set in production mode")
    val realm = System.getenv("JWT_REALM") ?: error("JWT_REALM must be set in production mode")

    return AppConfig.JwtConfig(secret, issuer, audience, realm)
}
