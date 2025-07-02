package com.example.domain.models

import java.time.Instant
import java.time.LocalTime
import java.util.UUID

/**
 * Course entity
 */
data class Course(
    override val id: UUID,
    val name: String,
    val lecturerId: UUID,
    val createdAt: Instant
) : BaseEntity

/**
 * Course schedule entity
 */
data class CourseSchedule(
    override val id: UUID,
    val courseId: UUID,
    val dayOfWeek: Int, // 1-7 for Monday-Sunday
    val startTime: LocalTime,
    val endTime: LocalTime,
    val roomNumber: String? = null,
    val meetingLink: String? = null
) : BaseEntity

/**
 * Status for course sessions
 */
enum class SessionStatus {
    SCHEDULED, ACTIVE, CLOSED, CANCELLED
}