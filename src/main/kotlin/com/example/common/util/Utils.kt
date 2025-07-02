package com.example.common.util

// Reuse the configuration loaded via AppConfig
val SECRET: String get() = AppConfig.jwtSecret
val ISSUER: String get() = AppConfig.jwtIssuer
val AUDIENCE: String get() = AppConfig.jwtAudience

//val PORT: String get() = jwtConfig.port

const val ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000 // 15 minutes
const val REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000 // 7 days