package com.example


import com.example.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


/**
 * Manual main function to provide more control over startup
 */
fun main() {
    val environment = System.getenv("ENVIRONMENT") ?: handleDefaultEnvironment()
    val config = extractConfig(environment, HoconApplicationConfig(ConfigFactory.load()))

    embeddedServer(Netty, port = config.port) {
        module {
            install(Koin) {
                slf4jLogger()
                modules(module {
//                    single<HelloService> {
//                        HelloService {
//                            println(environment.log.info("Hello, World!"))
//                        }
//                    }
                })
            }
        }
    }

}

fun handleDefaultEnvironment(): String {
    println("No environment specified, defaulting to 'dev'")
    return "dev"
}

fun extractConfig(environment: String, hoconConfig: HoconApplicationConfig): Config {
    val hoconEnvironment = hoconConfig.config("ktor.deployment.$environment")
    return Config(
        hoconEnvironment.property("host").getString(),
        Integer.parseInt(hoconEnvironment.property("port").getString()),
        hoconEnvironment.property("databaseHost").getString(),
        hoconEnvironment.property("databasePort").getString(),
    )
}