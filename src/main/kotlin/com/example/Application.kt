package com.example

import com.example.config.ApiConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Manual main function to provide more control over startup
 */
fun main() {
    val environment = System.getenv("ENVIRONMENT") ?: handleDefaultEnvironment()

    embeddedServer(Netty, port = apiConfig.serverPort, host = apiConfig.host) {
        module()
    }.start(wait = true)
}

fun handleDefaultEnvironment(): String {
    println("No environment specified, defaulting to 'dev'")
    return "dev"
}
