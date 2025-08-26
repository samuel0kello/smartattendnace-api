package com.example.di

import com.example.config.AppConfig
import com.example.services.auth.AuthService
import com.example.services.auth.TokenProvider
import com.example.services.auth.JwtConfig
import org.koin.dsl.module

val authModule = module {
    single<TokenProvider> {
        JwtConfig(get<AppConfig>().jwtSecret)
    }
    single {
        AuthService(get())
    }
}