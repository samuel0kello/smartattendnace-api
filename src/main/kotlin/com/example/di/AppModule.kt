package com.example.di

import com.example.config.ApiConfig
import com.example.config.JwtConfig
import com.example.config.PostgresConfig
import com.example.config.core.EnvConfigurationLoader
import com.example.services.auth.JwtTokenManager
import com.example.services.auth.TokenProvider
import org.koin.dsl.module

val appModule = module {
    // Config
    single {  }
    
    // Database
//    single {
//        DatabaseProvider(
//            host = config.databaseHost,
//            port = config.databasePort,
//            databaseName = config.dbName,
//            user = config.dbUser,
//            password = config.dbPassword
//        )
//    }
//
//    // Auth
//    single<TokenProvider> { JwtConfig(config.jwtSecret) }
//    single { AuthService(get()) }
    
    // Add other services here
}

val configModule = module {
    single { EnvConfigurationLoader.load(ApiConfig::class) }
    single { EnvConfigurationLoader.load(PostgresConfig::class) }
    single { EnvConfigurationLoader.load(JwtConfig::class) }
}

val securityModule = module {
    single <TokenProvider> {
        val jwtConfig: JwtConfig = get()
        JwtTokenManager(
            jwtConfig.jwtSecrete,
            jwtConfig.jwtIssuer,
            jwtConfig.jwtAudience,
            jwtConfig.jwtRealm,
        )
    }
}