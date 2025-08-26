package com.example.config

import kotlinx.serialization.Serializable

@Serializable
data class SecurityConfig(
    val jwt: JwtConfig
)


@Serializable
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)
