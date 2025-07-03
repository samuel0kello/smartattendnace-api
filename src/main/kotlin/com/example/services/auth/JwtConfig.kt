package com.example.services.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.database.entity.User
import com.example.model.CredentialsResponse
import java.util.*

class JwtConfig(private val secret: String) : TokenProvider {
    private val issuer = "smart-attendance-api"
    private val validityInMs: Long = 15 * 60 * 1000 // 15 minutes
    private val refreshValidityInMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
    private val algorithm = Algorithm.HMAC512(secret)

    private val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    override fun verifyToken(token: String): String? {
        return try {
            verifier.verify(token).claims["id"]?.asString()
        } catch (e: Exception) {
            null
        }
    }

    override fun getVerifier(): JWTVerifier = verifier

    override fun createToken(user: User) = CredentialsResponse(
        createToken(user, getTokenExpiration(), "access"),
        createToken(user, getTokenExpiration(refreshValidityInMs), "refresh")
    )

    private fun createToken(user: User, expiration: Date, tokenType: String) = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", user.id.value.toString())
        .withClaim("email", user.email)
        .withClaim("role", user.role.name)
        .withClaim("type", tokenType)
        .withExpiresAt(expiration)
        .sign(algorithm)

    // calculate expiration date based on current time + the given validity
    private fun getTokenExpiration(validity: Long = validityInMs) = Date(System.currentTimeMillis() + validity)
}

interface TokenProvider {
    fun createToken(user: User): CredentialsResponse
    fun verifyToken(token: String): String?
    fun getVerifier(): JWTVerifier
}