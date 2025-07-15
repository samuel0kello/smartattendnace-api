package com.example.config

import com.example.config.core.Configuration
import com.example.config.core.ConfigurationProperty

@Configuration("api")
data class ApiConfig(
    @ConfigurationProperty("SERVER-PORT")
    val serverPort: Int,
    @ConfigurationProperty("HOST")
    val host: String,
)
