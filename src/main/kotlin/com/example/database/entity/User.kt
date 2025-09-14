package com.example.database.entity

import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import java.util.*
import kotlin.time.ExperimentalTime

enum class UserRole {
    ADMIN,
    LECTURER,
    STUDENT
}

object Users : UUIDTable("Users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val role = enumeration("role", UserRole::class)
    //check { it inList listOf("ADMIN", "LECTURER", "STUDENT") }
    val employerId = varchar("employer_id", 50).nullable()
    val registrationNumber = varchar("registration_number", 50).nullable()
    val isActive = bool("is_active").default(false)
    val profilePicture = varchar("profile_picture", 255).nullable()
    @OptIn(ExperimentalTime::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val emailVerified = bool("email_verified").default(false)
    val emailVerificationToken = varchar("email_verification_token", 255).nullable()
    @OptIn(ExperimentalTime::class)
    val emailVerificationTokenExpiry = timestamp("email_verification_token_expiry").nullable()
    val passwordResetToken = varchar("password_reset_token", 255).nullable()
    @OptIn(ExperimentalTime::class)
    val passwordResetTokenExpiry = timestamp("password_reset_token_expiry").nullable()
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
    @OptIn(ExperimentalTime::class) var createdAt by Users.createdAt
    @OptIn(ExperimentalTime::class) var updatedAt by Users.updatedAt
    var emailVerified by Users.emailVerified
    var emailVerificationToken by Users.emailVerificationToken
    @OptIn(ExperimentalTime::class) var emailVerificationTokenExpiry by Users.emailVerificationTokenExpiry
    var passwordResetToken by Users.passwordResetToken
    @OptIn(ExperimentalTime::class) var passwordResetTokenExpiry by Users.passwordResetTokenExpiry

}