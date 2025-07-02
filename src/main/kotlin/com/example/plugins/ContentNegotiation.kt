package com.example.plugins

import com.example.common.responses.ApiResponse
import com.example.features.auth.models.AdminSignUpRequest
import com.example.features.auth.models.LecturerSignUpRequest
import com.example.features.auth.models.SignUpRequest
import com.example.features.auth.models.StudentSignUpRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Configure content negotiation for JSON serialization
 */
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true

            serializersModule = SerializersModule {
                polymorphic(SignUpRequest::class) {
                    subclass(StudentSignUpRequest::class)
                    subclass(LecturerSignUpRequest::class)
                    subclass(AdminSignUpRequest::class)
                }
            }
        })
    }
}