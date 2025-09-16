package com.example.domain.model

import com.example.shared.UUIDSerializer
import kotlinx.datetime.LocalTime
import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class CourseModel(
    val courseId: Int,
    val title: String,
    val description: String?,
    val credits: Int,
    @Serializable(with = UUIDSerializer::class)
    val lecturerId: java.util.UUID,
    val schedules: List<CourseScheduleModel>,
    @OptIn(ExperimentalTime::class) @Contextual val createdAt: Instant,
    @OptIn(ExperimentalTime::class) @Contextual val updatedAt: Instant
): ResponseData()

@Serializable
data class CourseScheduleModel(
    val scheduleId: Int,
    val dayOfWeek: String?,
    @Contextual val startTime: LocalTime,
    val endTime: LocalTime,
    val frequency: String?,
    @OptIn(ExperimentalTime::class) @Contextual val createdAt: Instant,
    @OptIn(ExperimentalTime::class) @Contextual val updatedAt: Instant
)

@Serializable
data class CreateCourseRequest(
    val title: String,
    val description: String?,
    val credits: Int,
)

@Serializable
data class CreateCourseResponse(
    val courseId: Int,
    val message: String
)
