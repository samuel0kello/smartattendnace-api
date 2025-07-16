package com.example

import com.example.plugins.configureDI
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun main(args: Array<String>) {

    val env = System.getenv("KTOR_ENV") ?: "dev"
    dotenv {
        directory = "./config/env"
        filename = ".env.$env"
        ignoreIfMissing = false
        ignoreIfMalformed = false
    }
}


fun Application.module() {
    configureDI()

    routing {
        get ("/") {
            call.respond("Hello World!")
        }
    }
}