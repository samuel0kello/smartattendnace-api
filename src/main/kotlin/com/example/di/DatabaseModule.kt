package com.example.di

import com.example.config.AppConfig
import com.example.config.DatabaseConfig
import com.example.database.DatabaseProvider
import org.koin.dsl.module

val databaseModule = module {
    single {
        get<AppConfig>().database
    }

    single {
        DatabaseProvider(get<DatabaseConfig>())
    }
}