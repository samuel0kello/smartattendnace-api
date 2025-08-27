package com.example.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val deployment: DeploymentConfig,
    val security: SecurityConfig,
    val database: DatabaseConfig,
    val smtp: SmtpConfig,
    )