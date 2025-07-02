//package com.example.plugins
//
//import com.example.config.AppConfig
//import io.ktor.http.*
//import io.ktor.server.application.*
//import io.ktor.server.plugins.cors.routing.*
//import mu.KotlinLogging
//
//private val logger = KotlinLogging.logger {}
//
///**
// * Configure CORS
// */
//fun Application.configureCors() {
//    logger.info { "Configuring CORS..." }
//
//    install(CORS) {
//        allowMethod(HttpMethod.Options)
//        allowMethod(HttpMethod.Get)
//        allowMethod(HttpMethod.Post)
//        allowMethod(HttpMethod.Put)
//        allowMethod(HttpMethod.Delete)
//        allowHeader(HttpHeaders.ContentType)
//        allowHeader(HttpHeaders.Authorization)
//        allowHeader(HttpHeaders.AccessControlAllowOrigin)
//        allowHeader("X-Requested-With")
//
//        // Allow credentials (cookies, auth headers)
//        allowCredentials = true
//
//        // Maximum age for preflight requests cache
//        maxAgeInSeconds = 3600
//
//        // In development, allow all hosts
//        if (AppConfig.server.isDevelopment) {
//            logger.warn { "Running in development mode - CORS allowing any host" }
//            anyHost()
//        } else {
//            // In production, specify allowed origins
//            logger.info { "Running in production mode - CORS restricted to specific origins" }
//            allowHost("*.smartattendance.com", schemes = listOf("https"))
//            allowHost("smartattendance-app.azurewebsites.net", schemes = listOf("https"))
//            allowHost("unpkg.com", schemes = listOf("https"))  // Allow Swagger UI resources
//            // Add more allowed origins as needed
//        }
//    }
//}