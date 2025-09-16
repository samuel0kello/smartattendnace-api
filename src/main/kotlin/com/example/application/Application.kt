package com.example.application

import com.example.config.AppConfig
import com.example.config.ConfigurationProvider
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*


fun main() {
    val config = ApplicationConfig("application.yaml")
        .property("ktor")
        .getAs<AppConfig>()

    ConfigurationProvider.appConfig = config

    embeddedServer(Netty, port = config.deployment.port, host = config.deployment.host){
        module()
    }.start(wait = true)
}
