package com.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection
import com.example.database.entity.Users

class DatabaseProvider(
    private val host: String,
    private val port: String,
    private val databaseName: String,
    private val user: String,
    private val password: String
) {
    fun init() {
        createDatabaseIfNotExists()

        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://$host:$port/$databaseName?useSSL=false"
            username = user
            password = this@DatabaseProvider.password
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
            jdbcUrl = "jdbc:postgresql://$host:$port/postgres?useSSL=false"  // Connect to default database
            username = user
            password = this@DatabaseProvider.password
            maximumPoolSize = 1
            isAutoCommit = true
        }

        HikariDataSource(postgresConfig).use { dataSource ->
            dataSource.connection.use { connection ->
                connection.autoCommit = true
                val databaseExists = checkIfDatabaseExists(connection, databaseName)

                if (!databaseExists) {
                    println("Database '$databaseName' does not exist. Creating it now.")
                    val statement = connection.createStatement()
                    statement.execute("CREATE DATABASE $databaseName")
                    statement.close()
                    println("Database '$databaseName' created successfully.")
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