package com.example

import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.java.KoinJavaComponent.inject

/**
 * Manual main function to provide more control over startup
 */
fun main() {
    val apiConfig by inject<ApiConfig>(ApiConfig::class.java)
    embeddedServer(Netty, port = apiConfig.serverPort, host = apiConfig.host) {
        module()
    }.start(wait = true)
}

fun handleDefaultEnvironment(): String {
    println("No environment specified, defaulting to 'dev'")
    return "dev"
}
