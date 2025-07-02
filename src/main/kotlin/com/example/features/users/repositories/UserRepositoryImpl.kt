package com.example.features.users.repositories

import com.example.di.dbQuery
import com.example.domain.models.User
import com.example.domain.models.UserRole
import com.example.domain.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository
 */
@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {
    
    override suspend fun getById(id: UUID): User? = dbQuery {
        Users.select { Users.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }
    
    override suspend fun getByEmail(email: String): User? = dbQuery {
        Users.select { Users.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }
    
    override suspend fun getAll(): List<User> = dbQuery {
        Users.selectAll()
            .map { it.toUser() }
    }
    
    override suspend fun create(entity: User): User = dbQuery {
        val insertStatement = Users.insert {
            it[id] = entity.id
            it[name] = entity.name
            it[email] = entity.email
            it[password] = entity.password
            it[role] = entity.role
            it[createdAt] = entity.createdAt
            it[updatedAt] = entity.updatedAt
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toUser()
            ?: throw Exception("Failed to insert user")
    }
    
    override suspend fun update(entity: User): Boolean = dbQuery {
        val updatedRows = Users.update({ Users.id eq entity.id }) {
            it[name] = entity.name
            it[email] = entity.email
            it[password] = entity.password
            it[role] = entity.role
            it[updatedAt] = Instant.now()
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = Users.deleteWhere { Users.id eq id }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to User model
     */
    private fun ResultRow.toUser(): User = User(
        id = this[Users.id].value,
        name = this[Users.name],
        email = this[Users.email],
        password = this[Users.password],
        role = this[Users.role],
        createdAt = this[Users.createdAt],
        updatedAt = this[Users.updatedAt]
    )
}
