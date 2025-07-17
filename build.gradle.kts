plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.1.20"
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.smart-attendance"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

    tasks.withType<Jar> {
        manifest {
            attributes["Main-Class"] = "com.example.ApplicationKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

}

repositories {
    mavenCentral()
}

tasks {
    shadowJar {
        archiveBaseName.set("smartAttendance")
        archiveClassifier.set("all")
        archiveVersion.set("") // Optional: Removes the version from the filename
    }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("io.ktor:ktor-server-host-common:3.1.1")
    implementation("io.ktor:ktor-server-auth:3.1.1")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.kotlinx.serialization)

    implementation(libs.reflection)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.43.0") // For timestamps

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("io.ktor:ktor-server-swagger:2.3.5")
    implementation("io.ktor:ktor-server-openapi:2.3.5")
    implementation("io.ktor:ktor-server-cors:2.3.5")

    // QR Code Generation (for attendance)
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1")

    implementation("io.ktor:ktor-server-auth-jwt:2.3.6")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.6")

    //swagger
    implementation("io.ktor:ktor-server-swagger:3.1.1")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("io.ktor:ktor-server-status-pages:3.1.1")

    //koin
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-ktor:3.5.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.0")



    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:3.1.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
    testImplementation("io.mockk:mockk:1.13.8")

}