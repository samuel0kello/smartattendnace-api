package com.example.database.entity

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.Date
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.UUID

enum class UserRole {
    ADMIN,
    LECTURER,
    STUDENT
}

object Users : UUIDTable() {
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val passwordHash: Column<String> = varchar("password_hash", 255)
    val firstName: Column<String> = varchar("first_name", 100)
    val lastName: Column<String> = varchar("last_name", 100)
    val role: Column<UserRole> = enumeration("role", UserRole::class)
    val employerId: Column<String?> = varchar("employer_id", 50).nullable()
    val registrationNumber: Column<String?> = varchar("registration_number", 50).nullable()
    val isActive: Column<Boolean> = bool("is_active").default(true)
    val profilePicture: Column<String?> = varchar("profile_picture", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val emailVerified = bool("email_verified").default(false)
    val emailVerificationToken = varchar("email_verification_token", 255).nullable()
    val emailVerificationTokenExpiry = datetime("email_verification_token_expiry").nullable()
    val passwordResetToken = varchar("password_reset_token", 255).nullable()
    val passwordResetTokenExpiry = datetime("password_reset_token_expiry").nullable()
}


class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var email by Users.email
    var passwordHash by Users.passwordHash
    var firstName by Users.firstName
    var lastName by Users.lastName
    var role by Users.role
    var employerId by Users.employerId
    var registrationNumber by Users.registrationNumber
    var isActive by Users.isActive
    var profilePicture by Users.profilePicture
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
    var emailVerified by Users.emailVerified
    var emailVerificationToken by Users.emailVerificationToken
    var emailVerificationTokenExpiry by Users.emailVerificationTokenExpiry
    var passwordResetToken by Users.passwordResetToken
    var passwordResetTokenExpiry by Users.passwordResetTokenExpiry

// Helper properties to check user type
    val isAdmin get() = role == UserRole.ADMIN
    val isLecturer get() = role == UserRole.LECTURER
    val isStudent get() = role == UserRole.STUDENT
}