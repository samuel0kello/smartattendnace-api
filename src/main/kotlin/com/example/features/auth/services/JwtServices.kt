package com.example.features.auth.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.example.config.AppConfig
import com.example.domain.models.User
import com.example.domain.models.UserRole
import mu.KotlinLogging
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * Service for JWT token operations
 */
@Singleton
class JwtService @Inject constructor() {
    private val config = AppConfig.jwt
    private val algorithm = Algorithm.HMAC256(config.secret)
    
    /**
     * Get JWT verifier for token validation
     */
    fun getJWTVerifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()
    
    /**
     * Generate an access token
     */
    fun generateAccessToken(userId: String, role: UserRole): String {
        logger.debug { "Generating access token for user $userId with role $role" }
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withClaim("userId", userId)
            .withClaim("role", role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + config.accessTokenExpirationMs))
            .sign(algorithm)
    }
    
    /**
     * Generate a refresh token
     */
    fun generateRefreshToken(userId: String, role: UserRole): String {
        logger.debug { "Generating refresh token for user $userId with role $role" }
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withClaim("userId", userId)
            .withClaim("role", role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + config.refreshTokenExpirationMs))
            .sign(algorithm)
    }
    
    /**
     * Generate a token pair for a user
     */
    fun generateTokens(user: User): TokenPair {
        val userId = user.id.toString()
        return TokenPair(
            accessToken = generateAccessToken(userId, user.role),
            refreshToken = generateRefreshToken(userId, user.role)
        )
    }
    
    /**
     * Verify and decode a token
     */
    fun verifyToken(token: String): DecodedJWT? = try {
        getJWTVerifier().verify(token)
    } catch (e: Exception) {
        logger.warn { "Token verification failed: ${e.message}" }
        null
    }
    
    /**
     * Extract claims from a JWT token
     */
    fun extractClaims(token: String): TokenClaims? {
        val decodedJWT = verifyToken(token) ?: return null
        
        val userId = decodedJWT.getClaim("userId").asString() ?: return null
        val roleString = decodedJWT.getClaim("role").asString() ?: return null
        val role = try {
            UserRole.valueOf(roleString)
        } catch (e: IllegalArgumentException) {
            logger.warn { "Invalid role in token: $roleString" }
            return null
        }
        
        return TokenClaims(userId, role)
    }
    
    /**
     * Data class for token pair
     */
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )
    
    /**
     * Data class for token claims
     */
    data class TokenClaims(
        val userId: String,
        val role: UserRole
    )
}
