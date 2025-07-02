package com.example.application

import com.example.config.AppConfig
import com.example.di.AppInjector
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * Application module - entry point for the application
 */
fun Application.module() {
    try {
        // Initialize database first
        logger.info { "Initializing database..." }
        AppInjector.databaseFactory.init()
        logger.info { "Database initialized successfully" }

        // Install plugins in the correct order - important for proper functioning
        configureContentNegotiation()
        configureStatusPages()
        configureAuthentication()
        configureSwagger()
//        configureCors()

        // Register routes
        configureRouting()

        logger.info { "Application started successfully" }
        logger.info { "Server running on ${environment.config.propertyOrNull("ktor.deployment.host")?.getString() ?: "0.0.0.0"}:${environment.config.propertyOrNull("ktor.deployment.port")?.getString() ?: "8080"}" }
        logger.info { "Swagger UI available at /swagger" }
    } catch (e: Exception) {
        logger.error(e) { "Failed to initialize application: ${e.message}" }
        throw e  // Rethrow to prevent application from starting with initialization errors
    }
}

/**
 * Manual main function to provide more control over startup
 */
fun main() {
    try {
        // Load configuration and log it
        AppConfig.logConfiguration()

        // Start the server
        logger.info { "Starting HTTP server on ${AppConfig.server.host}:${AppConfig.server.port}" }

        embeddedServer(
            factory = Netty,
            port = AppConfig.server.port,
            host = AppConfig.server.host,
            module = Application::module
        ).start(wait = true)
    } catch (e: Exception) {
        logger.error(e) { "Failed to start application: ${e.message}" }
        exitProcess(1)  // Exit with error code
    }
}