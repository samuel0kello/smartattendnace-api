package com.example.application.plugins

import com.example.di.IClosableComponent
import com.example.di.appModule
import com.example.di.authModule
import com.example.di.courseModule
import com.example.di.databaseModule
import com.example.di.emailModule
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

private val logger = KotlinLogging.logger {}


fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()

        modules(
            appModule(environment.config),
            databaseModule,
            authModule,
            courseModule,
            emailModule
        )

        createEagerInstances()
    }

    // Monitor application lifecycle events
    monitor.subscribe(ApplicationStarting) {
        logger.info { "Koin Application starting" }
    }

    monitor.subscribe(ApplicationStarted) {
        logger.info { "Koin Application started" }
    }

    monitor.subscribe(ApplicationStopping) {
        logger.info { "Shutdown started" }

        val closableComponents = getKoin().getAll<IClosableComponent>()
        closableComponents.forEach {
            runBlocking {
                it.close()
            }
        }
    }

    monitor.subscribe(ApplicationStopped) {
        logger.info { "Shutdown completed gracefully" }
    }
}