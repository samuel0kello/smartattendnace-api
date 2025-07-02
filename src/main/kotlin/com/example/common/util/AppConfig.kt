package com.example.common.util

import java.io.File
import java.util.*

object AppConfig {
    // JWT Configuration
    val jwtSecret: String
    val jwtIssuer: String
    val jwtAudience: String
    val jwtRealm: String

    // Database Configuration
    val dbUrl: String
    val dbUsername: String
    val dbPassword: String

    // App Configuration
    val isDevelopment: Boolean
    val port: Int

    init {
        println("Initializing application configuration...")

        // Load properties from local.properties file for development
        val properties = Properties()

        // Try to load local properties file (for development)
        val localPropertiesFile = File("local.properties")
        println("Checking for local.properties at: ${localPropertiesFile.absolutePath}")
        println("File exists: ${localPropertiesFile.exists()}")

        if (localPropertiesFile.exists()) {
            try {
                localPropertiesFile.inputStream().use {
                    properties.load(it)
                    println("Loaded local.properties configuration file")
                    println("Properties loaded: ${properties.size}")
                    println("Available keys: ${properties.keys.joinToString()}")
                }
            } catch (e: Exception) {
                println("Error loading properties file: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("No local.properties file found, falling back to environment variables")
            // Debug: Print all environment variables
            println("Available environment variables:")
            System.getenv().forEach { (key, value) ->
                // Print the key and first character of value (for security)
                val maskedValue = if (value.isNotEmpty()) "${value.first()}..." else "empty"
                println("  $key: $maskedValue")
            }
        }

        // Environment determination
        isDevelopment = getConfigValue("ENVIRONMENT", properties, "environment", "development") != "production"
        println("Running in ${if (isDevelopment) "DEVELOPMENT" else "PRODUCTION"} mode")

        // Get JWT configuration
        jwtSecret = getConfigValue("JWT_SECRET", properties, "jwt.secret", defaultIfDev = "development-secret-key")
        jwtIssuer = getConfigValue("JWT_ISSUER", properties, "jwt.issuer", defaultIfDev = "SmartAttendanceApi")
        jwtAudience = getConfigValue("JWT_AUDIENCE", properties, "jwt.audience", defaultIfDev = "SmartAttendanceApi")
        jwtRealm = getConfigValue("JWT_REALM", properties, "jwt.realm", defaultIfDev = "smart-attendance-api")

        // Database config - handle different naming conventions
        dbUrl = getConfigValue("DB_URL", properties, "db.url", defaultIfDev = "jdbc:mysql://host.docker.internal:3306/smart_attendance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC")
        dbUsername = getConfigValue("DB_USERNAME", properties, "db.username", defaultIfDev = "root")
        dbPassword = getConfigValue("DB_PASSWORD", properties, "db.password", defaultIfDev = "root")

        // Other configs
        port = getConfigValue("PORT", properties, "port", "8080").toInt()

        println("Configuration loaded successfully")
        println("DEBUG - JWT Issuer: $jwtIssuer")
        println("DEBUG - JWT Audience: $jwtAudience")
        println("DEBUG - JWT Realm: $jwtRealm")
        println("DEBUG - DB URL: $dbUrl")
        println("DEBUG - DB Username: $dbUsername (masked)")
        println("DEBUG - Port: $port")
    }

    private fun getConfigValue(
        envKey: String,
        properties: Properties,
        propKey: String? = null,
        defaultValue: String? = null,
        required: Boolean = false,
        defaultIfDev: String? = null
    ): String {
        // First try exact environment variable match
        val fromEnv = System.getenv(envKey)
        if (!fromEnv.isNullOrBlank()) {
            println("Found environment variable: $envKey")
            return fromEnv
        }

        // Then try property file
        if (propKey != null) {
            val fromProps = properties.getProperty(propKey)
            if (!fromProps.isNullOrBlank()) {
                println("Found property: $propKey")
                return fromProps
            }
        }

        // Use development default if applicable
        if (isDevelopment && defaultIfDev != null) {
            println("Using development default for: $envKey")
            return defaultIfDev
        }

        // Use general default if provided
        if (defaultValue != null) {
            println("Using default value for: $envKey")
            return defaultValue
        }

        // If required and not found, throw error
        if (required) {
            val errorMsg = "Required configuration value $envKey not found in environment variables or properties file"
            println("ERROR: $errorMsg")
            throw IllegalStateException(errorMsg)
        }

        // Return empty string as last resort
        println("WARNING: No value found for $envKey, using empty string")
        return ""
    }
}