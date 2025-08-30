package com.example.di

import com.example.config.AppConfig
import com.example.services.email.EmailService
import com.example.services.email.EmailVerificationService
import org.koin.dsl.module

val emailModule = module {
    single { get<AppConfig>().smtp}
    single{EmailService(get())}
    single{ EmailVerificationService(get())}
}