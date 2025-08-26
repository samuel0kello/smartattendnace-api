package com.example.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val host: String,
    val port: Int,
    val database: DatabaseConfig,
    val jwtSecret: String,
)