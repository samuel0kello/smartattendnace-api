package com.example.config

import kotlinx.serialization.Serializable

@Serializable
data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromEmail: String = username, // Default sender email will be the username
    val ssl: Boolean = true, // Default to using SSL
    val starttls: Boolean = true // Default to using STARTTLS
)
