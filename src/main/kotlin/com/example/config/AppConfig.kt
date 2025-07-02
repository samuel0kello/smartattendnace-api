package com.example.config

import io.github.cdimascio.dotenv.dotenv
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Type-safe application configuration using environment variables with fallback to .env file
 */
object AppConfig {
    // Load environment variables or .env file
    private val dotenv = dotenv {
        ignoreIfMissing = true
        systemProperties = true
    }

    // JWT Configuration
    data class JwtConfig(
        val secret: String,
        val issuer: String,
        val audience: String,
        val realm: String,
        val accessTokenExpirationMs: Long = 24 * 60 * 60 * 1000,  // 24 hours
        val refreshTokenExpirationMs: Long = 7 * 24 * 60 * 60 * 1000  // 7 days
    )

    // Database Configuration
    data class DatabaseConfig(
        val url: String,
        val username: String,
        val password: String,
        val driverClassName: String = "com.mysql.cj.jdbc.Driver",
        val maxPoolSize: Int = 10,
        val minIdle: Int = 5
    )

    // Server Configuration
    data class ServerConfig(
        val port: Int,
        val host: String = "0.0.0.0",
        val isDevelopment: Boolean
    )

    // Initialize configurations
    val jwt = JwtConfig(
        secret = getConfigValue("JWT_SECRET", "development-secret-key", required = !isDevelopment()),
        issuer = getConfigValue("JWT_ISSUER", "SmartAttendanceApi"),
        audience = getConfigValue("JWT_AUDIENCE", "SmartAttendanceApi"),
        realm = getConfigValue("JWT_REALM", "smart-attendance-api")
    )

    val database = DatabaseConfig(
        url = getConfigValue(
            "DB_URL",
            "jdbc:mysql://localhost:3306/smart_attendance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        ),
        username = getConfigValue("DB_USERNAME", "root"),
        password = getConfigValue("DB_PASSWORD", "root")
    )

    val server = ServerConfig(
        port = getConfigValue("PORT", "8080").toInt(),
        isDevelopment = isDevelopment()
    )

    /**
     * Determine if the application is running in development mode
     */
    private fun isDevelopment(): Boolean {
        return getConfigValue("ENVIRONMENT", "development") != "production"
    }

    /**
     * Get configuration value with fallback
     */
    private fun getConfigValue(
        key: String,
        defaultValue: String? = null,
        required: Boolean = false
    ): String {
        val value = dotenv[key] ?: System.getenv(key) ?: defaultValue

        if (value == null && required) {
            val errorMsg = "Required configuration value $key not found"
            logger.error { errorMsg }
            throw IllegalStateException(errorMsg)
        }

        return value ?: ""
    }

    /**
     * Log configuration on startup (excluding sensitive values)
     */
    fun logConfiguration() {
        logger.info { "Application Configuration:" }
        logger.info { "- Environment: ${if (server.isDevelopment) "DEVELOPMENT" else "PRODUCTION"}" }
        logger.info { "- Server: ${server.host}:${server.port}" }
        logger.info { "- JWT Issuer: ${jwt.issuer}" }
        logger.info { "- JWT Audience: ${jwt.audience}" }
        logger.info { "- Database URL: ${database.url}" }
        logger.info { "- Database User: ${database.username}" }
    }
}