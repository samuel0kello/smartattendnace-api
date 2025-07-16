package com.example.plugins

import com.example.di.IClosableComponent
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.slf4jLogger

private val logger = KotlinLogging.logger {}

/**
 * configure dependency injection and graceful shutdown
 */
fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()

        modules( /*modules */)

        this.createEagerInstances()
    }

    environment.monitor.subscribe(ApplicationStarted) { logger.error { "Koin Application started" } }

    environment.monitor.subscribe(KoinApplicationStopPreparing) {
        logger.error { "Shutdown started" }

        val closableComponents by lazy {
            getKoin().getAll<IClosableComponent>()
        }

        closableComponents.forEach {
            runBlocking {
                it.close()
            }
        }
    }

    environment.monitor.subscribe(KoinApplicationStopped) { logger.error { "Shutdown completed gracefully" } }

}