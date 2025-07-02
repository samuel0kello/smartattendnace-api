package com.example.features.courses.repositories

import com.example.domain.models.CourseSchedule
import java.util.UUID

/**
 * Course schedule repository interface
 */
interface CourseScheduleRepository {
    suspend fun getByCourseId(courseId: UUID): List<CourseSchedule>
    suspend fun getById(id: UUID): CourseSchedule?
    suspend fun create(schedule: CourseSchedule): CourseSchedule
    suspend fun update(schedule: CourseSchedule): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun deleteByCourseId(courseId: UUID): Boolean
}
