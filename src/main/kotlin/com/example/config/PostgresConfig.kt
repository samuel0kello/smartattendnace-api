package com.example.config

import com.example.config.core.Configuration
import com.example.config.core.ConfigurationProperty

@Configuration("postgres")
data class PostgresConfig(
    @ConfigurationProperty("url")
    val url: String,
    @ConfigurationProperty("user")
    val user: String,
    @ConfigurationProperty("password")
    val password: String,
)
