package com.example.domain.tables

import com.example.domain.models.UserRole
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Users table definition
 */
object Users : UUIDTable("users") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val password = text("password") // Hashed password
    val role = enumerationByName("role", 10, UserRole::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").nullable()
}

/**
 * Students table definition (extends Users)
 */
object Students : Table("students") {
    val userId = reference("user_id", Users.id)
    val regNo = varchar("reg_no", 255).uniqueIndex()
    val department = varchar("department", 100).nullable()
    val yearOfStudy = integer("year_of_study").nullable()

    override val primaryKey = PrimaryKey(userId)
}

/**
 * Staff table definition (extends Users for Lecturers and Admins)
 */
object Staff : Table("staff") {
    val userId = reference("user_id", Users.id)
    val employeeId = varchar("employee_id", 255).uniqueIndex()
    val department = varchar("department", 100).nullable()
    val position = varchar("position", 100).nullable()

    override val primaryKey = PrimaryKey(userId)
}
