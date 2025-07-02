package com.example.domain.models

import java.time.Instant
import java.util.UUID

/**
 * Assignment entity
 */
data class Assignment(
    override val id: UUID,
    val courseId: UUID,
    val lecturerId: UUID,
    val title: String,
    val description: String,
    val dueDate: Instant,
    val createdAt: Instant
) : BaseEntity

/**
 * Submission entity
 */
data class Submission(
    override val id: UUID,
    val studentId: UUID,
    val assignmentId: UUID,
    val fileUrl: String,
    val submissionDate: Instant,
    val grade: Float? = null
) : BaseEntity

/**
 * Grade entity
 */
data class Grade(
    override val id: UUID,
    val studentId: UUID,
    val assignmentId: UUID,
    val score: Float,
    val feedback: String? = null,
    val gradedAt: Instant
) : BaseEntity