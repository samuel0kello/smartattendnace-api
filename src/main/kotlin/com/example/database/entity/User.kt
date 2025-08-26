package com.example.database.entity

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
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
    val createdAt: Column<Long> = long("created_at")
    val updatedAt: Column<Long> = long("updated_at")
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

    // Helper properties to check user type
    val isAdmin get() = role == UserRole.ADMIN
    val isLecturer get() = role == UserRole.LECTURER
    val isStudent get() = role == UserRole.STUDENT
}