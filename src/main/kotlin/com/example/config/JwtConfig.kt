package com.example.config

import com.example.config.core.Configuration
import com.example.config.core.ConfigurationProperty

@Configuration("jwt")
data class JwtConfig(
    @ConfigurationProperty(name = "SECRETE")
    val jwtSecrete : String,

    @ConfigurationProperty("ISSUER")
    val jwtIssuer: String,

    @ConfigurationProperty("AUDIENCE")
    val jwtAudience: String,

    @ConfigurationProperty("REALM", optional = true)
    val jwtRealm: String? = "smart-attendance-api",
)