package com.example.features.auth.repositories

import com.example.domain.models.User
import com.example.domain.repositories.BaseRepository
import java.util.UUID

/**
 * Authentication repository interface
 */
interface AuthRepository {
    suspend fun findUserByEmail(email: String): User?
}