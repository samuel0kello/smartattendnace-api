package com.example.di

import dagger.Module
import dagger.Provides
import com.example.features.auth.services.JwtService
import com.example.features.auth.util.RoleAuthorization
import com.example.features.auth.util.RoleAuthorizationImpl
import javax.inject.Singleton

/**
 * Core application module
 */
@Module
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabaseFactory(): DatabaseFactory {
        return DatabaseFactory()
    }
    
    @Provides
    @Singleton
    fun provideJwtService(): JwtService {
        return JwtService()
    }
    
    @Provides
    @Singleton
    fun provideRoleAuthorization(): RoleAuthorization {
        return RoleAuthorizationImpl()
    }
}