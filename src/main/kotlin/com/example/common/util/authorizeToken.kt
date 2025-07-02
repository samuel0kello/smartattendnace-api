package com.example.common.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.models.UserRole
import com.example.domain.tables.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Generalized function to validate a user's token, check role authorization,
 * and optionally check if the user exists in the system.
 *
 * @param call The application call containing the request data.
 * @param secret The secret key used to validate the JWT token.
 * @param allowedRoles Allowed roles for the resource.
 * @param checkUserExistence Whether to verify that the user exists in the system.
 * @return A pair of userId and userRole if authorization is successful, null otherwise.
 */
suspend fun authorizeToken(
    call: ApplicationCall,
    secret: String,
    allowedRoles: Set<UserRole>,
    checkUserExistence: Boolean = true
): Pair<String, UserRole>? {
    val token = extractToken(call) ?: return unauthorized(call, "Authorization token is required")
    val decodedJWT = validateToken(token, secret) ?: return unauthorized(call, "Invalid or expired token")

    val userId = decodedJWT.getClaim("userId")?.asString()
        ?: return unauthorized(call, "Token is missing userId")
    val userRole = decodedJWT.getClaim("role")?.asString()?.let { UserRole.valueOf(it) }
        ?: return unauthorized(call, "Token is missing or has an invalid role")

    if (userRole !in allowedRoles) {
        return unauthorized(call, "Insufficient permissions")
    }

    if (checkUserExistence && !isUserExists(userId, userRole)) {
        return unauthorized(call, "User does not exist or has an invalid role")
    }

    return Pair(userId, userRole)
}

private fun extractToken(call: ApplicationCall): String? {
    val authHeader = call.request.headers["Authorization"] ?: return null
    if (!authHeader.startsWith("Bearer ")) return null
    return authHeader.removePrefix("Bearer ")
}

private fun validateToken(token: String, secret: String): com.auth0.jwt.interfaces.DecodedJWT? {
    return try {
        JWT.require(Algorithm.HMAC256(secret)).build().verify(token)
    } catch (e: Exception) {
        null
    }
}

private fun isUserExists(userId: String, expectedRole: UserRole): Boolean {
    return transaction {
        Users.select { (Users.id eq UUID.fromString(userId)) and (Users.role eq expectedRole) }
            .empty()
            .not()
    }
}

private suspend fun unauthorized(call: ApplicationCall, message: String): Nothing? {
    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to message))
    return null
}