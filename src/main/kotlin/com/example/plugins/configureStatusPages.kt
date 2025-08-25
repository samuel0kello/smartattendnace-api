package com.example.plugins

import com.example.util.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
    exception<Throwable> { call, cause ->

        val errorMessage = cause.message ?: "Something went wrong"
        val stackTrace = cause.stackTraceToString()

        //include stack trace for dev env
        val isDevelopment = System.getenv("ENVIRONMENT")?.lowercase() != "production"
        val detailedError = if (isDevelopment) "$errorMessage\n$stackTrace" else errorMessage

        call.respond(
            HttpStatusCode.InternalServerError,
            ApiResponse(
                success = false,
                message = "Something went wrong",
                data = null,
                error = detailedError,
            )
        )
    }
}
}