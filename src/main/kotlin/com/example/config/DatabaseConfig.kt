package com.example.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val databaseName: String,
    val username: String,
    val password: String,
    val jdbcUrl: String = "jdbc:postgresql://$host:$port/$databaseName"
)
