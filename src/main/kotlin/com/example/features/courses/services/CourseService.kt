package com.example.features.courses.services

import com.example.common.exceptions.BadRequestException
import com.example.common.exceptions.ForbiddenException
import com.example.common.exceptions.NotFoundException
import com.example.domain.models.Course
import com.example.domain.models.CourseSchedule
import com.example.domain.models.UserRole
import com.example.features.courses.models.*
import com.example.features.courses.repositories.CourseRepository
import com.example.features.courses.repositories.CourseScheduleRepository
import com.example.features.users.repositories.UserRepository
import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * Course service interface
 */
interface CourseService {
    suspend fun getCourseById(id: UUID): CourseResponseDto
    suspend fun getAllCourses(): List<CourseResponseDto>
    suspend fun getCoursesByLecturer(lecturerId: UUID): List<CourseResponseDto>
    suspend fun getCoursesForStudent(studentId: UUID): List<CourseResponseDto>
    suspend fun createCourse(lecturerId: UUID, request: CreateCourseRequest): CourseResponseDto
    suspend fun updateCourse(id: UUID, userId: UUID, userRole: UserRole, request: UpdateCourseRequest): CourseResponseDto
    suspend fun deleteCourse(id: UUID, userId: UUID, userRole: UserRole): Boolean

    // New admin-specific methods
    suspend fun adminCreateCourse(adminId: UUID, request: AdminCourseCreateRequest): CourseResponseDto
    suspend fun adminAssignLecturerToCourse(adminId: UUID, courseId: UUID, lecturerId: UUID): CourseResponseDto
    suspend fun getAvailableLecturers(): List<LecturerDto>
}

/**
 * Implementation of CourseService
 */
@Singleton
class CourseServiceImpl @Inject constructor(
    private val courseRepository: CourseRepository,
    private val courseScheduleRepository: CourseScheduleRepository,
    private val userRepository: UserRepository
) : CourseService {

    /**
     * Get a course by ID with its schedules
     */
    override suspend fun getCourseById(id: UUID): CourseResponseDto {
        val course = courseRepository.getById(id)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(course.lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        val schedules = courseScheduleRepository.getByCourseId(id)
            .map { CourseScheduleDto.fromCourseSchedule(it) }

        return CourseResponseDto.fromCourse(
            course = course,
            lecturerName = lecturer.name,
            schedules = schedules
        )
    }

    /**
     * Get all courses with their schedules
     */
    override suspend fun getAllCourses(): List<CourseResponseDto> {
        val courses = courseRepository.getAll()

        return courses.map { course ->
            val lecturer = userRepository.getById(course.lecturerId)
                ?: return@map null // Skip courses with missing lecturers

            val schedules = courseScheduleRepository.getByCourseId(course.id)
                .map { CourseScheduleDto.fromCourseSchedule(it) }

            CourseResponseDto.fromCourse(
                course = course,
                lecturerName = lecturer.name,
                schedules = schedules
            )
        }.filterNotNull()
    }

    /**
     * Get courses taught by a specific lecturer
     */
    override suspend fun getCoursesByLecturer(lecturerId: UUID): List<CourseResponseDto> {
        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        if (lecturer.role != UserRole.LECTURER && lecturer.role != UserRole.ADMIN) {
            throw BadRequestException("User is not a lecturer")
        }

        val courses = courseRepository.getCoursesByLecturerId(lecturerId)

        return courses.map { course ->
            val schedules = courseScheduleRepository.getByCourseId(course.id)
                .map { CourseScheduleDto.fromCourseSchedule(it) }

            CourseResponseDto.fromCourse(
                course = course,
                lecturerName = lecturer.name,
                schedules = schedules
            )
        }
    }

    /**
     * Get courses in which a student is enrolled
     */
    override suspend fun getCoursesForStudent(studentId: UUID): List<CourseResponseDto> {
        val student = userRepository.getById(studentId)
            ?: throw NotFoundException("Student not found")

        if (student.role != UserRole.STUDENT) {
            throw BadRequestException("User is not a student")
        }

        // In a real implementation, this would use an enrollment repository
        // For now, we'll just return an empty list
        return emptyList()
    }

    /**
     * Create a new course with schedules
     */
    override suspend fun createCourse(lecturerId: UUID, request: CreateCourseRequest): CourseResponseDto {
        logger.info { "Creating course ${request.name} for lecturer $lecturerId" }

        // Validate request
        if (request.name.isBlank()) {
            throw BadRequestException("Course name cannot be blank")
        }

        // Verify lecturer exists
        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        if (lecturer.role != UserRole.LECTURER && lecturer.role != UserRole.ADMIN) {
            throw BadRequestException("User is not a lecturer")
        }

        // Create course
        val course = Course(
            id = UUID.randomUUID(),
            name = request.name,
            lecturerId = lecturerId,
            createdAt = Instant.now()
        )

        val createdCourse = courseRepository.create(course)
        logger.info { "Created course with ID: ${createdCourse.id}" }

        // Create schedules if provided
        val schedules = request.schedules?.mapNotNull { scheduleRequest ->
            try {
                val dayOfWeek = DayOfWeek.valueOf(scheduleRequest.dayOfWeek.uppercase()).value
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val startTime = LocalTime.parse(scheduleRequest.startTime, timeFormatter)
                val endTime = LocalTime.parse(scheduleRequest.endTime, timeFormatter)

                val schedule = CourseSchedule(
                    id = UUID.randomUUID(),
                    courseId = createdCourse.id,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    roomNumber = scheduleRequest.roomNumber,
                    meetingLink = scheduleRequest.meetingLink
                )

                courseScheduleRepository.create(schedule)
                CourseScheduleDto.fromCourseSchedule(schedule)
            } catch (e: IllegalArgumentException) {
                logger.warn { "Invalid day of week: ${scheduleRequest.dayOfWeek}" }
                null
            } catch (e: DateTimeParseException) {
                logger.warn { "Invalid time format: ${e.message}" }
                null
            }
        }

        return CourseResponseDto.fromCourse(
            course = createdCourse,
            lecturerName = lecturer.name,
            schedules = schedules
        )
    }

    /**
     * Update a course
     */
    override suspend fun updateCourse(id: UUID, userId: UUID, userRole: UserRole, request: UpdateCourseRequest): CourseResponseDto {
        logger.info { "Updating course $id" }

        // Get existing course
        val existingCourse = courseRepository.getById(id)
            ?: throw NotFoundException("Course not found")

        // Check permissions (only the lecturer who created the course or an admin can update it)
        if (userRole != UserRole.ADMIN && existingCourse.lecturerId != userId) {
            throw ForbiddenException("You don't have permission to update this course")
        }

        // Update course
        val updatedCourse = existingCourse.copy(
            name = request.name ?: existingCourse.name
        )

        if (!courseRepository.update(updatedCourse)) {
            throw Exception("Failed to update course")
        }

        // Get lecturer name
        val lecturer = userRepository.getById(updatedCourse.lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        // Get schedules
        val schedules = courseScheduleRepository.getByCourseId(id)
            .map { CourseScheduleDto.fromCourseSchedule(it) }

        return CourseResponseDto.fromCourse(
            course = updatedCourse,
            lecturerName = lecturer.name,
            schedules = schedules
        )
    }

    /**
     * Delete a course and its schedules
     */
    override suspend fun deleteCourse(id: UUID, userId: UUID, userRole: UserRole): Boolean {
        logger.info { "Deleting course $id" }

        // Get existing course
        val existingCourse = courseRepository.getById(id)
            ?: throw NotFoundException("Course not found")

        // Check permissions (only the lecturer who created the course or an admin can delete it)
        if (userRole != UserRole.ADMIN && existingCourse.lecturerId != userId) {
            throw ForbiddenException("You don't have permission to delete this course")
        }

        // Delete schedules first
        courseScheduleRepository.deleteByCourseId(id)

        // Delete course
        return courseRepository.delete(id)
    }

    /**
     * Admin-specific method to create a course and assign a lecturer
     */
    override suspend fun adminCreateCourse(adminId: UUID, request: AdminCourseCreateRequest): CourseResponseDto {
        logger.info { "Admin $adminId creating course ${request.name}" }

        // Verify admin exists and has admin role
        val admin = userRepository.getById(adminId)
            ?: throw NotFoundException("Admin not found")

        if (admin.role != UserRole.ADMIN) {
            throw ForbiddenException("Only admins can use this endpoint")
        }

        // Validate request
        if (request.name.isBlank()) {
            throw BadRequestException("Course name cannot be blank")
        }

        // Verify lecturer exists if provided
        val lecturerId = if (request.lecturerId != null) {
            val lecturerUUID = UUID.fromString(request.lecturerId)
            val lecturer = userRepository.getById(lecturerUUID)
                ?: throw NotFoundException("Lecturer not found")

            if (lecturer.role != UserRole.LECTURER) {
                throw BadRequestException("Assigned user is not a lecturer")
            }

            lecturerUUID
        } else {
            // If no lecturer is assigned, the admin is temporarily the course owner
            adminId
        }

        // Create course
        val course = Course(
            id = UUID.randomUUID(),
            name = request.name,
            lecturerId = lecturerId,
            createdAt = Instant.now()
        )

        val createdCourse = courseRepository.create(course)
        logger.info { "Admin created course with ID: ${createdCourse.id}" }

        // Create schedules if provided
        val schedules = request.schedules?.mapNotNull { scheduleRequest ->
            try {
                val dayOfWeek = DayOfWeek.valueOf(scheduleRequest.dayOfWeek.uppercase()).value
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val startTime = LocalTime.parse(scheduleRequest.startTime, timeFormatter)
                val endTime = LocalTime.parse(scheduleRequest.endTime, timeFormatter)

                val schedule = CourseSchedule(
                    id = UUID.randomUUID(),
                    courseId = createdCourse.id,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    roomNumber = scheduleRequest.roomNumber,
                    meetingLink = scheduleRequest.meetingLink
                )

                courseScheduleRepository.create(schedule)
                CourseScheduleDto.fromCourseSchedule(schedule)
            } catch (e: IllegalArgumentException) {
                logger.warn { "Invalid day of week: ${scheduleRequest.dayOfWeek}" }
                null
            } catch (e: DateTimeParseException) {
                logger.warn { "Invalid time format: ${e.message}" }
                null
            }
        }

        // Get lecturer name
        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        return CourseResponseDto.fromCourse(
            course = createdCourse,
            lecturerName = lecturer.name,
            schedules = schedules
        )
    }

    /**
     * Admin-specific method to assign a lecturer to a course
     */
    override suspend fun adminAssignLecturerToCourse(adminId: UUID, courseId: UUID, lecturerId: UUID): CourseResponseDto {
        logger.info { "Admin $adminId assigning lecturer $lecturerId to course $courseId" }

        // Verify admin exists and has admin role
        val admin = userRepository.getById(adminId)
            ?: throw NotFoundException("Admin not found")

        if (admin.role != UserRole.ADMIN) {
            throw ForbiddenException("Only admins can use this endpoint")
        }

        // Verify course exists
        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        // Verify lecturer exists
        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        if (lecturer.role != UserRole.LECTURER) {
            throw BadRequestException("Assigned user is not a lecturer")
        }

        // Update course with new lecturer
        val updatedCourse = course.copy(
            lecturerId = lecturerId
        )

        if (!courseRepository.update(updatedCourse)) {
            throw Exception("Failed to update course")
        }

        logger.info { "Admin assigned lecturer $lecturerId to course $courseId" }

        // Get schedules
        val schedules = courseScheduleRepository.getByCourseId(courseId)
            .map { CourseScheduleDto.fromCourseSchedule(it) }

        return CourseResponseDto.fromCourse(
            course = updatedCourse,
            lecturerName = lecturer.name,
            schedules = schedules
        )
    }

    /**
     * Get all available lecturers in the system
     */
    override suspend fun getAvailableLecturers(): List<LecturerDto> {
        logger.info { "Getting all available lecturers" }

        // Get all users with LECTURER role
        return userRepository.getAll()
            .filter { it.role == UserRole.LECTURER }
            .map { lecturer ->
                LecturerDto(
                    id = lecturer.id.toString(),
                    name = lecturer.name,
                    email = lecturer.email
                )
            }
    }
}