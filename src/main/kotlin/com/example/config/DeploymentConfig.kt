package com.example.config

import kotlinx.serialization.Serializable

@Serializable
data class DeploymentConfig(
    val host: String,
    val port: Int,
    val baseUrl: String
)
