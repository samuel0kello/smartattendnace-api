package com.example.config.core

import io.ktor.server.config.ApplicationConfig
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

object ConfigLoader {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> load(config: ApplicationConfig, clazz: KClass<T>): T {
        val configAnnotation = clazz.findAnnotation<Configuration>()
            ?: throw IllegalArgumentException("Missing @Configuration on ${clazz.simpleName}")

        val prefix = configAnnotation.prefix

        val primaryConstructor = clazz.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor for ${clazz.simpleName}")

        val parameters = primaryConstructor.parameters.associateWith { param ->
            val propAnn = param.findAnnotation<ConfigurationProperty>()
                ?: throw IllegalArgumentException("Missing @ConfigurationProperty on ${param.name} in ${clazz.simpleName}")

            val key = listOf("ktor", prefix, propAnn.name).joinToString(".")
            val rawValue = config.propertyOrNull(key)?.getString()

            if (rawValue == null) {
                if (!propAnn.optional) error("Missing required configurationfor $key")
                else null
            } else {
                convert(rawValue, param.type.jvmErasure)
            }
        }

        return primaryConstructor.callBy(parameters)
    }

    private fun convert(value: String, type: KClass<*>): Any = when (type) {
        String::class -> value
        Int::class -> value.toInt()
        Long::class -> value.toLong()
        Double::class -> value.toDouble()
        Float::class -> value.toFloat()
        Boolean::class -> value.toBoolean()
        else -> throw IllegalArgumentException("Unsupported type $type")
    }
}