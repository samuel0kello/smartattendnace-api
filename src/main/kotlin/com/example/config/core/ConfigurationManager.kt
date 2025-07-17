package com.example.config.core

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

private val logger = KotlinLogging.logger {}

interface IConfigManager {
    fun <T : Any> getConfig(clazz: KClass<T>): T
    fun scanAndLoadAllConfigs()
}

class DefaultConfigManager(
    private val configLoader: ConfigLoader,
    private val configPackage: String = DEFAULT_CONFIG_PACKAGE
) : IConfigManager, com.example.di.IConfigManager {

    companion object {
        const val DEFAULT_CONFIG_PACKAGE = "com.example.config"
    }

    private val configCache = ConcurrentHashMap<KClass<*>, Any>()

    init {
        scanAndLoadAllConfigs()
    }

    override fun <T : Any> getConfig(clazz: KClass<T>): T {
        logger.debug { "Attempting to retrieve config for ${clazz.simpleName} from cache." }
        @Suppress("UNCHECKED_CAST")
        return configCache.computeIfAbsent(clazz) {
            logger.info { "Config for ${clazz.simpleName} not found in cache. Loading it now." }
            configLoader.load(it as KClass<T>)
        } as T
    }

    override fun scanAndLoadAllConfigs() {
        logger.info { "Starting automatic scan for configuration classes in package '$configPackage'." }
        val reflections = Reflections(ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(configPackage))
            .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes))

        val configClasses = reflections.getTypesAnnotatedWith(Configuration::class.java)

        if (configClasses.isEmpty()) {
            logger.warn { "No classes annotated with @Configuration found in package '$configPackage'." }
            return
        }

        logger.info { "Found ${configClasses.size} configuration classes. Loading them." }
        configClasses.forEach { clazz ->
            try {
                // Ensure it's a Kotlin class and has a primary constructor for our loader
                val kClass = clazz.kotlin
                if (kClass.findAnnotation<Configuration>() != null && kClass.primaryConstructor != null) {
                    logger.debug { "Loading configuration for class: ${kClass.simpleName}" }
                    // Load and cache each configuration found
                    val configInstance = configLoader.load(kClass)
                    configCache[kClass] = configInstance
                    logger.info { "Successfully loaded and cached configuration for ${kClass.simpleName}." }
                } else {
                    logger.warn { "Skipping ${kClass.simpleName}: Not a valid Kotlin class or missing primary constructor for configuration loading." }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load configuration for class ${clazz.simpleName}: ${e.message}" }
            }
        }
        logger.info { "Completed scanning and loading all configurations." }
    }
}