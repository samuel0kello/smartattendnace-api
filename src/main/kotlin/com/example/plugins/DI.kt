package com.example.plugins

import com.example.di.appModule
import com.example.di.configModule
import com.example.di.securityModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule, configModule, securityModule)
    }
}