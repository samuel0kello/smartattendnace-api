package com.example.config

class Config(
    val host: String,
    val port: Int,
    val databaseHost: String,
    val databasePort: String,
) {
    companion object {
        const val DATABASE_NAME: String = "smart_attendance"
        const val DATABASE_USER: String = "root"
        const val DATABASE_PASSWORD: String = "smartattendnace@p123"
    }
}