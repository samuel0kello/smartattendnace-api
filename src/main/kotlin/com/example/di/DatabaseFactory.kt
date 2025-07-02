package com.example.di

import com.example.config.AppConfig
import com.example.domain.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * Database connection manager and transaction utilities
 */
@Singleton
class DatabaseFactory @Inject constructor() {
    /**
     * Initialize the database connection pool and schema
     */
    fun init() {
        try {
            logger.info { "Creating database connection pool..." }

            // Create database connection
            val dataSource = createHikariDataSource()
            Database.connect(dataSource)

            logger.info { "Connected to database successfully" }

            // Create database schema
            createTables()

            logger.info { "Database schema created successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize database: ${e.message}" }
            throw e
        }
    }

    /**
     * Create the database tables
     */
    private fun createTables() {
        transaction {
            SchemaUtils.create(
                // User-related tables
                Users,
                Students,
                Staff,

                // Course-related tables
                Courses,
                CourseSchedules,

                // Attendance-related tables
                AttendanceSessions,
                Attendance,
                AttendanceVerifications,

                // Assignment-related tables
                Assignments,
                Submissions,
                Grades,

                // Other tables
                Lectures,
                Notifications
            )
        }
    }

    /**
     * Create and configure the HikariCP connection pool
     */
    private fun createHikariDataSource(): HikariDataSource {
        val config = AppConfig.database

        return HikariDataSource(HikariConfig().apply {
            driverClassName = config.driverClassName
            jdbcUrl = config.url
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minIdle
            idleTimeout = 30000
            connectionTimeout = 30000
            maxLifetime = 1800000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000
        })
    }
}

/**
 * Execute database operations in a coroutine-friendly way
 */
suspend fun <T> dbQuery(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }