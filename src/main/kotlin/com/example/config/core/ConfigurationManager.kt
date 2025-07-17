package com.example.config.core

import mu.KotlinLogging
import kotlin.reflect.KClass

class ConfigurationManager(
    private val packageName: String,
    private val configurationReader: (key:String, clazz: KClass<*>) -> Any
) {
    companion object {
        private val log = KotlinLogging.logger {}

        const val DEFAULT_CONFIG_PACKAGE = "com.example.config"
    }

    fun <T : Any> getConfigs(clazz: KClass<T>): T {
        // build the config key prefix using @Configuration annotation
        val configAnn = clazz.annotations.filterIsInstance<Configuration>()
            ?: throw IllegalArgumentException("Missing @Configuration on ${clazz.qualifiedName}")

        val prefix = "ktor.${configAnn.first().prefix}"

        // use the configuration reader to load and instantiate the configuration class
        val configObj = configurationReader(prefix, clazz) ?: throw IllegalArgumentException("Could not load config for prefix : $prefix")

        @Suppress("UNCHECKED_CAST")
        return configObj as T
    }
}
