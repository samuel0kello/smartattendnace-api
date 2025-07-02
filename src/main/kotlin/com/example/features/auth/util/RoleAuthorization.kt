package com.example.features.auth.util

import com.example.common.exceptions.ForbiddenException
import com.example.common.exceptions.UnauthorizedException
import com.example.domain.models.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import mu.KotlinLogging
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * Interface for role-based authorization
 */
interface RoleAuthorization {
    suspend fun requireRole(call: ApplicationCall, vararg roles: UserRole)
    suspend fun requireAdmin(call: ApplicationCall)
    suspend fun requireLecturer(call: ApplicationCall)
    suspend fun requireStudent(call: ApplicationCall)
    suspend fun requireAdminOrLecturer(call: ApplicationCall)
    suspend fun getUserId(call: ApplicationCall): String
    suspend fun getUserRole(call: ApplicationCall): UserRole
}

/**
 * Implementation of role-based authorization
 */
@Singleton
class RoleAuthorizationImpl @Inject constructor() : RoleAuthorization {
    
    /**
     * Require the user to have one of the specified roles
     */
    override suspend fun requireRole(call: ApplicationCall, vararg roles: UserRole) {
        val principal = call.principal<JWTPrincipal>()
            ?: throw UnauthorizedException("Authentication required")
        
        val roleString = principal.payload.getClaim("role").asString()
            ?: throw UnauthorizedException("Invalid token: missing role claim")
        
        try {
            val userRole = UserRole.valueOf(roleString)
            if (userRole !in roles) {
                logger.warn { "Access denied: User has role $userRole but required one of ${roles.joinToString()}" }
                throw ForbiddenException("Insufficient permissions")
            }
        } catch (e: IllegalArgumentException) {
            logger.warn { "Invalid role in token: $roleString" }
            throw UnauthorizedException("Invalid role in token")
        }
    }
    
    /**
     * Require the user to have ADMIN role
     */
    override suspend fun requireAdmin(call: ApplicationCall) {
        requireRole(call, UserRole.ADMIN)
    }
    
    /**
     * Require the user to have LECTURER role
     */
    override suspend fun requireLecturer(call: ApplicationCall) {
        requireRole(call, UserRole.LECTURER)
    }
    
    /**
     * Require the user to have STUDENT role
     */
    override suspend fun requireStudent(call: ApplicationCall) {
        requireRole(call, UserRole.STUDENT)
    }
    
    /**
     * Require the user to have either ADMIN or LECTURER role
     */
    override suspend fun requireAdminOrLecturer(call: ApplicationCall) {
        requireRole(call, UserRole.ADMIN, UserRole.LECTURER)
    }
    
    /**
     * Get the authenticated user's ID
     */
    override suspend fun getUserId(call: ApplicationCall): String {
        val principal = call.principal<JWTPrincipal>()
            ?: throw UnauthorizedException("Authentication required")
        
        return principal.payload.getClaim("userId").asString()
            ?: throw UnauthorizedException("Invalid token: missing userId claim")
    }
    
    /**
     * Get the authenticated user's role
     */
    override suspend fun getUserRole(call: ApplicationCall): UserRole {
        val principal = call.principal<JWTPrincipal>()
            ?: throw UnauthorizedException("Authentication required")
        
        val roleString = principal.payload.getClaim("role").asString()
            ?: throw UnauthorizedException("Invalid token: missing role claim")
        
        return try {
            UserRole.valueOf(roleString)
        } catch (e: IllegalArgumentException) {
            logger.warn { "Invalid role in token: $roleString" }
            throw UnauthorizedException("Invalid role in token")
        }
    }
}