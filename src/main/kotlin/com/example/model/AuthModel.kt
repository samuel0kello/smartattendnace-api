package com.example.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ResponseData

// Existing models
@Serializable
data class LoginCredentials(
    val email: String,
    val password: String
)

@Serializable
data class LoginTokenResponse(
    val credentials: CredentialsResponse
): ResponseData()

@Serializable
data class CredentialsResponse(
    val accessToken: String,
    val refreshToken: String
): ResponseData()

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UserRegistrationRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val employerId: String? = null,
    val registrationNumber: String? = null
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val employerId: String? = null,
    val registrationNumber: String? = null,
    val profilePicture: String? = null
): ResponseData()


data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetResponse(
    val success: Boolean,
    val message: String
): ResponseData()


data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)