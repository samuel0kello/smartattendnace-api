package com.example.util

import com.example.database.entity.UserRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

class AuthorizationException(message: String) : RuntimeException(message)

/**
 * Requires the authenticated user to have any of the specified roles
 */
suspend fun ApplicationCall.requireRole(vararg roles: UserRole) {
    val principal = this.principal<JWTPrincipal>() ?: throw AuthorizationException("Authentication required")

    val userRole = principal.getClaim("role", String::class)
        ?: throw AuthorizationException("Role information missing")

    if (!roles.any { it.name == userRole }) {
        throw AuthorizationException("Insufficient permissions. Required roles: ${roles.joinToString()}")
    }
}

// Extension functions for common role checks
suspend fun ApplicationCall.requireAdmin() = requireRole(UserRole.ADMIN)
suspend fun ApplicationCall.requireLecturer() = requireRole(UserRole.LECTURER)
suspend fun ApplicationCall.requireStudent() = requireRole(UserRole.STUDENT)
suspend fun ApplicationCall.requireStaff() = requireRole(UserRole.ADMIN, UserRole.LECTURER)

/**
 * Extension function to handle authorization exceptions
 */
suspend fun ApplicationCall.handleAuthorizationException(e: AuthorizationException) {
    this.respond(
        HttpStatusCode.Forbidden,
        mapOf("success" to false, "data" to null, "error" to e.message)
    )
}