package com.example.di

import com.example.config.AppConfig
import com.example.services.auth.AuthService
import com.example.services.auth.TokenProvider
import com.example.services.auth.JwtConfig
import com.example.services.email.EmailVerificationService
import org.koin.dsl.module

val authModule = module {
    single<TokenProvider> {
        JwtConfig(get<AppConfig>().security.jwt.secret)
    }
    single {
        AuthService(get(), get())
    }
}