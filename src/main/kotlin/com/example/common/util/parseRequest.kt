package com.example.common.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

suspend inline fun <reified T : Any> ApplicationCall.parseRequest(): T? {
    return try {
        println("Attempting to parse request to ${T::class.simpleName}")
        val result = this.receive<T>()
        println("Successfully parsed request")
        result
    } catch (e: Exception) {
        println("Failed to parse request: ${e.message}")
        e.printStackTrace()
        this.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Invalid request body format")
        )
        null
    }
}

suspend inline fun <reified T : Any> ApplicationCall.parseAndValidateRequest(
    validate: T.() -> Boolean,
    errorMessage: String
): T? {
    val request = this.parseRequest<T>() ?: return null
    if (!request.validate()) {
        this.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to errorMessage)
        )
        return null
    }
    return request
}