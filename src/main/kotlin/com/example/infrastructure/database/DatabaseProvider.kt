package com.example.infrastructure.database

import com.example.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import java.sql.Connection
import com.example.infrastructure.database.entity.Users
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DatabaseProvider(
    private val dbConfig: DatabaseConfig,
) {
    fun init() {
        createDatabaseIfNotExists()

        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/${dbConfig.databaseName}?useSSL=false"
            username = dbConfig.username
            password = this@DatabaseProvider.dbConfig.password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        // Create tables if they don't exist
        createTables()
    }

    private fun createTables() {
        transaction {
            SchemaUtils.create(Users)
            println("Database tables created/verified successfully")
        }
    }

    private fun createDatabaseIfNotExists() {
        val postgresConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/postgres?useSSL=false"
            username = dbConfig.username
            password = this@DatabaseProvider.dbConfig.password
            maximumPoolSize = 1
            isAutoCommit = true
        }

        HikariDataSource(postgresConfig).use { dataSource ->
            dataSource.connection.use { connection ->
                connection.autoCommit = true
                val databaseExists = checkIfDatabaseExists(connection, dbConfig.databaseName)

                if (!databaseExists) {
                    println("Database '${dbConfig.databaseName}' does not exist. Creating it now.")
                    val statement = connection.createStatement()
                    statement.execute("CREATE DATABASE ${dbConfig.databaseName}")
                    statement.close()
                    println("Database '${dbConfig.databaseName}' created successfully.")
                }
            }
        }
    }

    private fun checkIfDatabaseExists(connection: Connection, dbName: String): Boolean {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = '$dbName'").use { resultSet ->
                return resultSet.next()
            }
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }