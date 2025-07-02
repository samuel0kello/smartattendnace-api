package com.example.plugins

import com.example.common.exceptions.BadRequestException
import com.example.common.exceptions.NotFoundException
import com.example.common.responses.errorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Configure status pages for exception handling
 */
fun Application.configureStatusPages() {
    logger.info { "Configuring status pages..." }

    install(StatusPages) {
        // Handle serialization exceptions
        exception<SerializationException> { call, cause ->
            logger.warn(cause) { "Serialization error: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                errorResponse(
                    message = cause.message ?: "Invalid request body",
                    code = HttpStatusCode.BadRequest.value
                )
            )
        }

        // Handle bad request exceptions
        exception<BadRequestException> { call, cause ->
            logger.warn { "Bad request: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                errorResponse(
                    message = cause.message ?: "Bad request",
                    code = HttpStatusCode.BadRequest.value
                )
            )
        }

        // Handle not found exceptions
        exception<NotFoundException> { call, cause ->
            logger.warn { "Resource not found: ${cause.message}" }
            call.respond(
                HttpStatusCode.NotFound,
                errorResponse(
                    message = cause.message ?: "Resource not found",
                    code = HttpStatusCode.NotFound.value
                )
            )
        }

        // Handle unauthorized exceptions
        exception<com.example.common.exceptions.UnauthorizedException> { call, cause ->
            logger.warn { "Unauthorized access: ${cause.message}" }
            call.respond(
                HttpStatusCode.Unauthorized,
                errorResponse(
                    message = cause.message ?: "Unauthorized access",
                    code = HttpStatusCode.Unauthorized.value
                )
            )
        }

        // Handle forbidden exceptions
        exception<com.example.common.exceptions.ForbiddenException> { call, cause ->
            logger.warn { "Forbidden access: ${cause.message}" }
            call.respond(
                HttpStatusCode.Forbidden,
                errorResponse(
                    message = cause.message ?: "Forbidden access",
                    code = HttpStatusCode.Forbidden.value
                )
            )
        }

        // Handle conflict exceptions
        exception<com.example.common.exceptions.ConflictException> { call, cause ->
            logger.warn { "Resource conflict: ${cause.message}" }
            call.respond(
                HttpStatusCode.Conflict,
                errorResponse(
                    message = cause.message ?: "Resource conflict",
                    code = HttpStatusCode.Conflict.value
                )
            )
        }

        // Handle parameter conversion exceptions
        exception<ParameterConversionException> { call, cause ->
            logger.warn { "Parameter conversion error: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                errorResponse(
                    message = "Invalid parameter: ${cause.parameterName}",
                    code = HttpStatusCode.BadRequest.value
                )
            )
        }

        // Handle all other exceptions
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception: ${cause.message}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                errorResponse(
                    message = "An unexpected error occurred",
                    code = HttpStatusCode.InternalServerError.value
                )
            )
        }
    }
}