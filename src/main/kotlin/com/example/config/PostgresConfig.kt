package com.example.config

import com.example.config.core.Configuration
import com.example.config.core.ConfigurationProperty

@Configuration("postgres")
object PostgresConfig {

    @ConfigurationProperty("url")
    var url: String = "jdbc:postgresql://localhost:5432/postgres"

    @ConfigurationProperty("username")
    var username: String = "postgres"

    @ConfigurationProperty("password")
    var password: String = "<PASSWORD>"
}