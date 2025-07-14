package com.example.config

data class Config(
    val host: String,
    val port: Int,
    val databaseHost: String,
    val databasePort: String,
    val jwtSecret: String,
    val dbUser: String,
    val dbPassword: String,
    val dbName: String
)