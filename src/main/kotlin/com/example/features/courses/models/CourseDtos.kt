package com.example.features.courses.models

import com.example.domain.models.Course
import com.example.domain.models.CourseSchedule
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

/**
 * Course response DTO
 */
@Serializable
data class CourseResponseDto(
    val id: String,
    val name: String,
    val lecturerId: String,
    val lecturerName: String,
    val createdAt: String,
    val schedules: List<CourseScheduleDto>? = null
) {
    companion object {
        fun fromCourse(
            course: Course,
            lecturerName: String,
            schedules: List<CourseScheduleDto>? = null
        ): CourseResponseDto {
            return CourseResponseDto(
                id = course.id.toString(),
                name = course.name,
                lecturerId = course.lecturerId.toString(),
                lecturerName = lecturerName,
                createdAt = course.createdAt.toString(),
                schedules = schedules
            )
        }
    }
}

/**
 * Course schedule DTO
 */
@Serializable
data class CourseScheduleDto(
    val id: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val roomNumber: String? = null,
    val meetingLink: String? = null
) {
    companion object {
        fun fromCourseSchedule(schedule: CourseSchedule): CourseScheduleDto {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val dayOfWeekName = DayOfWeek.of(schedule.dayOfWeek).name

            return CourseScheduleDto(
                id = schedule.id.toString(),
                dayOfWeek = dayOfWeekName,
                startTime = schedule.startTime.format(timeFormatter),
                endTime = schedule.endTime.format(timeFormatter),
                roomNumber = schedule.roomNumber,
                meetingLink = schedule.meetingLink
            )
        }
    }
}

/**
 * Create course request DTO
 */
@Serializable
data class CreateCourseRequest(
    val name: String,
    val schedules: List<CreateScheduleRequest>? = null
)

/**
 * Create schedule request DTO
 */
@Serializable
data class CreateScheduleRequest(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val roomNumber: String? = null,
    val meetingLink: String? = null
)

/**
 * Update course request DTO
 */
@Serializable
data class UpdateCourseRequest(
    val name: String? = null
)

/**
 * Course student enrollment DTO
 */
@Serializable
data class CourseEnrollmentDto(
    val courseId: String,
    val studentId: String
)

/**
 * Admin course creation request DTO
 */
@Serializable
data class AdminCourseCreateRequest(
    val name: String,
    val lecturerId: String? = null,
    val schedules: List<CreateScheduleRequest>? = null
)

/**
 * Lecturer DTO for listing available lecturers
 */
@Serializable
data class LecturerDto(
    val id: String,
    val name: String,
    val email: String
)