package com.example

import com.example.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Manual main function to provide more control over startup
 */
fun main() {
    val environment = System.getenv("ENVIRONMENT") ?: handleDefaultEnvironment()
    val config = extractConfig(environment, HoconApplicationConfig(ConfigFactory.load()))

    embeddedServer(Netty, port = config.port, host = config.host) {
        module(config)
    }.start(wait = true)
}

fun handleDefaultEnvironment(): String {
    println("No environment specified, defaulting to 'dev'")
    return "dev"
}

fun extractConfig(environment: String, hoconConfig: HoconApplicationConfig): Config {
    val hoconEnvironment = hoconConfig.config("ktor.deployment.$environment")
    return Config(
        hoconEnvironment.property("host").getString(),
        Integer.parseInt(hoconEnvironment.property("port").getString()),
        hoconEnvironment.property("databaseHost").getString(),
        hoconEnvironment.property("databasePort").getString(),
        hoconEnvironment.property("jwtSecret").getString(),
        hoconEnvironment.property("dbUser").getString(),
        hoconEnvironment.property("dbPassword").getString(),
        hoconEnvironment.property("dbName").getString()
    )
}