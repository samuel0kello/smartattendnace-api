package com.example.di

import com.example.config.Config
import com.example.database.DatabaseProvider
import com.example.services.auth.AuthService
import com.example.services.auth.JwtConfig
import com.example.services.auth.TokenProvider
import org.koin.dsl.module

fun appModule(config: Config) = module {
    // Config
    single { config }
    
    // Database
    single { 
        DatabaseProvider(
            host = config.databaseHost,
            port = config.databasePort,
            databaseName = config.dbName,
            user = config.dbUser,
            password = config.dbPassword
        ) 
    }
    
    // Auth
    single<TokenProvider> { JwtConfig(config.jwtSecret) }
    single { AuthService(get()) }
    
    // Add other services here
}