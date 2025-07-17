package com.example.di

import com.example.config.core.DefaultConfigManager
import com.example.config.core.IConfigManager
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.ConfigLoader
import org.koin.core.module.Module
import org.koin.dsl.module
import org.reflections.util.QueryFunction.single

fun appModule(applicationConfig: ApplicationConfig): Module = module {
    single { applicationConfig }

    single { com.example.config.core.ConfigLoader(get()) }

    single <IConfigManager>{ DefaultConfigManager(get(), "com.example.config") }
}