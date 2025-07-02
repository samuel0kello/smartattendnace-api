package com.example.domain.tables

import com.example.domain.models.NotificationType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Notifications table definition
 */
object Notifications : UUIDTable("notifications") {
    val userId = reference("user_id", Users.id)
    val message = text("message")
    val type = enumerationByName("type", 15, NotificationType::class)
    val createdAt = timestamp("created_at")
    val read = bool("read").default(false)
}