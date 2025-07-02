package com.example.domain.models

import java.time.Instant
import java.util.UUID

/**
 * Types of notifications
 */
enum class NotificationType {
    ASSIGNMENT, ATTENDANCE, GENERAL
}

/**
 * Notification entity
 */
data class Notification(
    override val id: UUID,
    val userId: UUID,
    val message: String,
    val type: NotificationType,
    val createdAt: Instant,
    val read: Boolean = false
) : BaseEntity