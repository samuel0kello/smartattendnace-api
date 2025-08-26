package com.example.di

import com.example.config.AppConfig
import com.example.config.ConfigurationProvider
import io.ktor.server.config.*
import org.koin.dsl.module

fun appModule(config: ApplicationConfig) = module {
    single { ConfigurationProvider.appConfig }
}