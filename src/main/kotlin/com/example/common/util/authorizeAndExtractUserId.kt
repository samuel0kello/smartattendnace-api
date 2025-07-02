package com.example.common.util

import com.example.domain.models.UserRole
import io.ktor.server.application.*

/**
 * Helper function to wrap the authorization and extract user ID if authorized.
 *
 * @param secret The secret key to validate the JWT token.
 * @param requiredRole The specific role required for this authorization (e.g., ADMIN).
 * @return The user ID if authorized successfully, or null if authorization fails.
 */
suspend fun ApplicationCall.authorizeUser(secret: String, requiredRole: UserRole): String? {
    val result = authorizeToken(this, secret, setOf(requiredRole), checkUserExistence = true)
    return result?.first // Return only the userId
}