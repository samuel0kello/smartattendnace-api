package com.example.config.core

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configuration(
    val prefix: String
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigurationProperty(
    val name: String,
    val optional: Boolean = false,
)