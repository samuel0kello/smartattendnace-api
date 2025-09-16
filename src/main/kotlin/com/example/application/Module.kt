package com.example.application

import com.example.infrastructure.database.DatabaseProvider
import com.example.application.plugins.*
import io.ktor.server.application.*
import org.koin.ktor.ext.get

fun Application.module() {
    configureDI()

    // Initialize database
    val databaseProvider = get<DatabaseProvider>()
    databaseProvider.init()

    // Configure plugins
    configureSerialization()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureSwagger()

    // Configure routing
    configureRouting()
}