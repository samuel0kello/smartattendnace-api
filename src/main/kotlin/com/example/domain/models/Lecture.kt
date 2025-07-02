package com.example.domain.models

import java.time.Instant
import java.util.UUID

/**
 * Lecture entity
 */
data class Lecture(
    override val id: UUID,
    val courseId: UUID,
    val lecturerId: UUID,
    val topic: String,
    val date: Instant,
    val duration: Int // in minutes
) : BaseEntity