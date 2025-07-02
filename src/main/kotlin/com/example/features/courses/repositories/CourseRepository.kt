package com.example.features.courses.repositories

import com.example.domain.models.Course
import com.example.domain.repositories.BaseRepository
import java.util.UUID

/**
 * Course repository interface
 */
interface CourseRepository : BaseRepository<Course> {
    suspend fun getCoursesByLecturerId(lecturerId: UUID): List<Course>
    suspend fun getCoursesForStudent(studentId: UUID): List<Course>
}