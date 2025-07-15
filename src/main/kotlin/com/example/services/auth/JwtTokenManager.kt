package com.example.services.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.database.entity.User
import com.example.model.CredentialsResponse
import java.util.*
import java.util.concurrent.TimeUnit


interface TokenProvider {
    fun createTokens(user: User): CredentialsResponse
    fun getVerifier(): JWTVerifier
}

class JwtTokenManager(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtRealm: String,
) : TokenProvider {

    private val accessTokenValidityInMs: Long = TimeUnit.MINUTES.toMillis(15) // 15 minutes
    private val refreshTokenValidityInMs: Long = TimeUnit.DAYS.toMillis(7) // 7 days
    private val algorithm = Algorithm.HMAC512(jwtSecret)

    private val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .build()

    override fun getVerifier(): JWTVerifier = verifier

    override fun createTokens(user: User) = CredentialsResponse(
        createToken(user, accessTokenValidityInMs, "access"),
        createToken(user, refreshTokenValidityInMs, "refresh")
    )

    private fun createToken(user: User, validityMs: Long, tokenType: String): String {
        val expirationDate = Date(System.currentTimeMillis() + validityMs)

        return JWT.create()
            .withSubject("Authentication")
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", user.id.value.toString())
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withClaim("type", tokenType)
            .withExpiresAt(expirationDate)
            .sign(algorithm)
    }
}