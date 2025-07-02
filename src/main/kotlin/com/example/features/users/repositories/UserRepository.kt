package com.example.features.users.repositories

import com.example.domain.models.User
import com.example.domain.repositories.BaseRepository
import java.util.UUID

/**
 * User repository interface
 */
interface UserRepository : BaseRepository<User> {
    suspend fun getByEmail(email: String): User?
}