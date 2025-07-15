package com.example.config.core

import com.example.domain.exception.EnvLoadException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

object EnvConfigurationLoader {

    private val logger = mu.KotlinLogging.logger {  }

    /***
     * Loads the configuration from the environment variables of the specified KClass.
     *
     * @param KClass The Kotlin class representing the configuration data class.
     * @return An instance of the configuration data class, populated with the values from the environment variables..
     * @throws EnvLoadException If any of the required environment variables are missing.
     *
     */
    fun <T : Any> load(kClass: KClass<T>): T {
        logger.info ("Attempting to load configuration for ${kClass.simpleName} for env variables")

        val configAnnotation = kClass.findAnnotation<Configuration>()
            ?: throw EnvLoadException("Class ${kClass.simpleName} must be annotated with @Configuration to be loaded by EnvConfiguration Loader")

        val prefix = configAnnotation.prefix.uppercase()

        val primaryConstructor = kClass.constructors.firstOrNull()
            ?: throw EnvLoadException("Class ${kClass.simpleName} must have a primary constructor.")

        val constructorArgs = mutableMapOf<KParameter, Any?>()

        for (parameter in primaryConstructor.parameters) {
            val propAnnotation = parameter.findAnnotation<ConfigurationProperty>()
                ?: throw EnvLoadException(
                    "Parameter ${parameter.name} of class ${kClass.simpleName} must be annotated" +
                            " with @ConfigurationProperty to be loaded by EnvConfiguration Loader"
                )

            val propName = propAnnotation.name.uppercase()
            val isOptional = propAnnotation.optional

            val envVarName = "${prefix}_$propName"
            val enValue = System.getenv(envVarName)

            if (enValue == null){
                if (!isOptional) {
                    throw EnvLoadException(
                        "Required environemt variable '$envVarName' for property '${parameter.name}'" +
                                "of type ${parameter.type.jvmErasure.simpleName} is not set"
                    )
                }
                logger.info("Optional environment variable '$envVarName' is not set. Using default/null for '${parameter.name}")
            } else {
                try {
                    val convertedValue = convertValue(enValue, parameter.type.jvmErasure)
                    constructorArgs[parameter] = convertedValue
                    logger.info("loaded '$envVarName' as ${parameter.name} with value : $convertedValue ")
                } catch (e: EnvLoadException) {
                    throw EnvLoadException (
                        "Failed to convert environment variable '$envVarName' ('$enValue')" +
                                "to type ${parameter.type.jvmErasure.simpleName} for property ${parameter.name}. ${e.message}",e
                    )
                } catch (e: Exception) {
                    throw EnvLoadException(
                        "An unexpected error occured during convertion of '$envVarName' ('$enValue') " +
                                "for property '${parameter.name}' : ${e.message}"
                    )
                }
            }

        }

        try {
            val configInstance = primaryConstructor.callBy(constructorArgs)
            logger.info("Successfully Loaded configuration for ${kClass.simpleName}")
            return configInstance
        } catch (e: Exception) {
            throw EnvLoadException("Failed to instantiate ${kClass.simpleName} using provided environment variables.", e)
        }
    }

    private fun convertValue(value: String, targetKClass: KClass<*>): Any {
        return when (targetKClass) {
            String::class -> value
            Int::class -> value.toIntOrNull()
                ?: throw EnvLoadException("Invalid integer format for '$value")
            Long::class -> value.toLongOrNull()
                ?: throw EnvLoadException(" invalid long format fore '$value")
            Boolean::class -> value.toBooleanStrictOrNull()
                ?: throw EnvLoadException("Invalid boolean format for '$value'. Must be 'true' or 'false'")
            Float::class -> value.toFloatOrNull()
                ?: throw EnvLoadException("Invalid float format for '$value'")
            Double::class -> value.toDoubleOrNull()
                ?: throw EnvLoadException("Invalid double format for '$value'")
            else -> throw EnvLoadException("Unsupported type '${targetKClass.simpleName}' for environment variable convertion.")
        }
    }
}