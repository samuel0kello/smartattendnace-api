package com.example.features.assignments.services

import com.example.common.exceptions.BadRequestException
import com.example.common.exceptions.ForbiddenException
import com.example.common.exceptions.NotFoundException
import com.example.domain.models.Assignment
import com.example.domain.models.Grade
import com.example.domain.models.Submission
import com.example.domain.models.UserRole
import com.example.features.assignments.models.*
import com.example.features.assignments.repositories.AssignmentRepository
import com.example.features.assignments.repositories.GradeRepository
import com.example.features.assignments.repositories.SubmissionRepository
import com.example.features.courses.repositories.CourseRepository
import com.example.features.users.repositories.UserRepository
import mu.KotlinLogging
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * Assignment service interface
 */
interface AssignmentService {
    suspend fun createAssignment(lecturerId: UUID, request: CreateAssignmentRequest): AssignmentResponseDto
    suspend fun getAssignmentById(id: UUID): AssignmentResponseDto
    suspend fun getAssignmentsByCourse(courseId: UUID): List<AssignmentResponseDto>
    suspend fun getAssignmentsByLecturer(lecturerId: UUID): List<AssignmentResponseDto>
    suspend fun updateAssignment(id: UUID, userId: UUID, userRole: UserRole, request: UpdateAssignmentRequest): AssignmentResponseDto
    suspend fun deleteAssignment(id: UUID, userId: UUID, userRole: UserRole): Boolean

    suspend fun submitAssignment(assignmentId: UUID, studentId: UUID, request: CreateSubmissionRequest): SubmissionResponseDto
    suspend fun getSubmissionById(id: UUID): SubmissionResponseDto
    suspend fun getSubmissionsByAssignment(assignmentId: UUID): List<SubmissionResponseDto>
    suspend fun getSubmissionsByStudent(studentId: UUID): List<SubmissionResponseDto>

    suspend fun gradeSubmission(submissionId: UUID, request: GradeRequest, graderId: UUID): GradeResponseDto
    suspend fun getGradeById(id: UUID): GradeResponseDto
    suspend fun getGradesByAssignment(assignmentId: UUID): List<GradeResponseDto>
    suspend fun getGradesByStudent(studentId: UUID): List<GradeResponseDto>
}

/**
 * Implementation of AssignmentService
 */
@Singleton
class AssignmentServiceImpl @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val submissionRepository: SubmissionRepository,
    private val gradeRepository: GradeRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) : AssignmentService {

    /**
     * Create a new assignment
     */
    override suspend fun createAssignment(lecturerId: UUID, request: CreateAssignmentRequest): AssignmentResponseDto {
        logger.info { "Creating assignment for lecturer $lecturerId" }

        // Validate request
        if (request.courseId.isBlank()) {
            throw BadRequestException("Course ID is required")
        }

        if (request.title.isBlank()) {
            throw BadRequestException("Title is required")
        }

        if (request.description.isBlank()) {
            throw BadRequestException("Description is required")
        }

        // Parse due date
        val dueDate = try {
            Instant.parse(request.dueDate)
        } catch (e: Exception) {
            throw BadRequestException("Invalid due date format. Use ISO-8601 format.")
        }

        // Verify course and lecturer exist
        val courseId = UUID.fromString(request.courseId)
        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        // Check if lecturer owns the course
        if (course.lecturerId != lecturerId) {
            throw ForbiddenException("You don't have permission to create assignments for this course")
        }

        // Create assignment
        val assignment = Assignment(
            id = UUID.randomUUID(),
            courseId = courseId,
            lecturerId = lecturerId,
            title = request.title,
            description = request.description,
            dueDate = dueDate,
            createdAt = Instant.now()
        )

        val createdAssignment = assignmentRepository.create(assignment)
        logger.info { "Created assignment with ID: ${createdAssignment.id}" }

        return AssignmentResponseDto.fromAssignment(
            assignment = createdAssignment,
            courseName = course.name,
            lecturerName = lecturer.name
        )
    }

    /**
     * Get an assignment by ID
     */
    override suspend fun getAssignmentById(id: UUID): AssignmentResponseDto {
        val assignment = assignmentRepository.getById(id)
            ?: throw NotFoundException("Assignment not found")

        val course = courseRepository.getById(assignment.courseId)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(assignment.lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        return AssignmentResponseDto.fromAssignment(
            assignment = assignment,
            courseName = course.name,
            lecturerName = lecturer.name
        )
    }

    /**
     * Get assignments for a course
     */
    override suspend fun getAssignmentsByCourse(courseId: UUID): List<AssignmentResponseDto> {
        val course = courseRepository.getById(courseId)
            ?: throw NotFoundException("Course not found")

        val assignments = assignmentRepository.getAssignmentsByCourse(courseId)

        return assignments.mapNotNull { assignment ->
            val lecturer = userRepository.getById(assignment.lecturerId) ?: return@mapNotNull null

            AssignmentResponseDto.fromAssignment(
                assignment = assignment,
                courseName = course.name,
                lecturerName = lecturer.name
            )
        }
    }

    /**
     * Get assignments created by a lecturer
     */
    override suspend fun getAssignmentsByLecturer(lecturerId: UUID): List<AssignmentResponseDto> {
        val lecturer = userRepository.getById(lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        if (lecturer.role != UserRole.LECTURER && lecturer.role != UserRole.ADMIN) {
            throw BadRequestException("User is not a lecturer")
        }

        val assignments = assignmentRepository.getAssignmentsByLecturer(lecturerId)

        return assignments.mapNotNull { assignment ->
            val course = courseRepository.getById(assignment.courseId) ?: return@mapNotNull null

            AssignmentResponseDto.fromAssignment(
                assignment = assignment,
                courseName = course.name,
                lecturerName = lecturer.name
            )
        }
    }

    /**
     * Update an assignment
     */
    override suspend fun updateAssignment(id: UUID, userId: UUID, userRole: UserRole, request: UpdateAssignmentRequest): AssignmentResponseDto {
        logger.info { "Updating assignment $id" }

        // Get existing assignment
        val existingAssignment = assignmentRepository.getById(id)
            ?: throw NotFoundException("Assignment not found")

        // Check permissions (only the lecturer who created the assignment or an admin can update it)
        if (userRole != UserRole.ADMIN && existingAssignment.lecturerId != userId) {
            throw ForbiddenException("You don't have permission to update this assignment")
        }

        // Parse due date if provided
        val dueDate = if (request.dueDate != null) {
            try {
                Instant.parse(request.dueDate)
            } catch (e: Exception) {
                throw BadRequestException("Invalid due date format. Use ISO-8601 format.")
            }
        } else {
            existingAssignment.dueDate
        }

        // Update assignment
        val updatedAssignment = existingAssignment.copy(
            title = request.title ?: existingAssignment.title,
            description = request.description ?: existingAssignment.description,
            dueDate = dueDate
        )

        if (!assignmentRepository.update(updatedAssignment)) {
            throw Exception("Failed to update assignment")
        }

        // Get course and lecturer
        val course = courseRepository.getById(updatedAssignment.courseId)
            ?: throw NotFoundException("Course not found")

        val lecturer = userRepository.getById(updatedAssignment.lecturerId)
            ?: throw NotFoundException("Lecturer not found")

        return AssignmentResponseDto.fromAssignment(
            assignment = updatedAssignment,
            courseName = course.name,
            lecturerName = lecturer.name
        )
    }

    /**
     * Delete an assignment
     */
    override suspend fun deleteAssignment(id: UUID, userId: UUID, userRole: UserRole): Boolean {
        logger.info { "Deleting assignment $id" }

        // Get existing assignment
        val existingAssignment = assignmentRepository.getById(id)
            ?: throw NotFoundException("Assignment not found")

        // Check permissions (only the lecturer who created the assignment or an admin can delete it)
        if (userRole != UserRole.ADMIN && existingAssignment.lecturerId != userId) {
            throw ForbiddenException("You don't have permission to delete this assignment")
        }

        // Delete the assignment
        return assignmentRepository.delete(id)
    }

    /**
     * Submit an assignment
     */
    override suspend fun submitAssignment(assignmentId: UUID, studentId: UUID, request: CreateSubmissionRequest): SubmissionResponseDto {
        logger.info { "Submitting assignment $assignmentId for student $studentId" }

        // Validate request
        if (request.fileUrl.isBlank()) {
            throw BadRequestException("File URL is required")
        }

        // Verify assignment and student exist
        val assignment = assignmentRepository.getById(assignmentId)
            ?: throw NotFoundException("Assignment not found")

        val student = userRepository.getById(studentId)
            ?: throw NotFoundException("Student not found")

        if (student.role != UserRole.STUDENT) {
            throw BadRequestException("User is not a student")
        }

        // Check if due date has passed
        val now = Instant.now()
        if (now.isAfter(assignment.dueDate)) {
            logger.warn { "Assignment is past due date: $assignmentId" }
            // We could throw an exception here, but let's allow late submissions and let
            // the instructor decide what to do with them
        }

        // Check if the student already submitted this assignment
        val existingSubmission = submissionRepository.getByAssignmentAndStudent(assignmentId, studentId)

        // Create or update submission
        val submission = if (existingSubmission != null) {
            // Update existing submission
            val updatedSubmission = existingSubmission.copy(
                fileUrl = request.fileUrl,
                submissionDate = now
            )

            if (!submissionRepository.update(updatedSubmission)) {
                throw Exception("Failed to update submission")
            }

            updatedSubmission
        } else {
            // Create new submission
            val newSubmission = Submission(
                id = UUID.randomUUID(),
                studentId = studentId,
                assignmentId = assignmentId,
                fileUrl = request.fileUrl,
                submissionDate = now
            )

            submissionRepository.create(newSubmission)
        }

        logger.info { "Submitted assignment $assignmentId for student $studentId" }

        return SubmissionResponseDto.fromSubmission(
            submission = submission,
            studentName = student.name
        )
    }

    /**
     * Get a submission by ID
     */
    override suspend fun getSubmissionById(id: UUID): SubmissionResponseDto {
        val submission = submissionRepository.getById(id)
            ?: throw NotFoundException("Submission not found")

        val student = userRepository.getById(submission.studentId)
            ?: throw NotFoundException("Student not found")

        // Check if there's a grade for this submission
        val grade = gradeRepository.getByAssignmentAndStudent(submission.assignmentId, submission.studentId)

        return SubmissionResponseDto.fromSubmission(
            submission = submission,
            studentName = student.name,
            grade = grade
        )
    }

    /**
     * Get all submissions for an assignment
     */
    override suspend fun getSubmissionsByAssignment(assignmentId: UUID): List<SubmissionResponseDto> {
        val assignment = assignmentRepository.getById(assignmentId)
            ?: throw NotFoundException("Assignment not found")

        val submissions = submissionRepository.getByAssignment(assignmentId)

        return submissions.mapNotNull { submission ->
            val student = userRepository.getById(submission.studentId) ?: return@mapNotNull null

            // Check if there's a grade for this submission
            val grade = gradeRepository.getByAssignmentAndStudent(submission.assignmentId, submission.studentId)

            SubmissionResponseDto.fromSubmission(
                submission = submission,
                studentName = student.name,
                grade = grade
            )
        }
    }

    /**
     * Get all submissions by a student
     */
    override suspend fun getSubmissionsByStudent(studentId: UUID): List<SubmissionResponseDto> {
        val student = userRepository.getById(studentId)
            ?: throw NotFoundException("Student not found")

        val submissions = submissionRepository.getByStudent(studentId)

        return submissions.map { submission ->
            // Check if there's a grade for this submission
            val grade = gradeRepository.getByAssignmentAndStudent(submission.assignmentId, submission.studentId)

            SubmissionResponseDto.fromSubmission(
                submission = submission,
                studentName = student.name,
                grade = grade
            )
        }
    }

    /**
     * Grade a submission
     */
    override suspend fun gradeSubmission(submissionId: UUID, request: GradeRequest, graderId: UUID): GradeResponseDto {
        logger.info { "Grading submission $submissionId by grader $graderId" }

        // Validate request
        if (request.score < 0 || request.score > 100) {
            throw BadRequestException("Score must be between 0 and 100")
        }

        // Get submission
        val submission = submissionRepository.getById(submissionId)
            ?: throw NotFoundException("Submission not found")

        // Get assignment and verify grader is the lecturer or an admin
        val assignment = assignmentRepository.getById(submission.assignmentId)
            ?: throw NotFoundException("Assignment not found")

        val grader = userRepository.getById(graderId)
            ?: throw NotFoundException("Grader not found")

        if (grader.role != UserRole.ADMIN && assignment.lecturerId != graderId) {
            throw ForbiddenException("Only the lecturer who created the assignment or an admin can grade submissions")
        }

        // Get student
        val student = userRepository.getById(submission.studentId)
            ?: throw NotFoundException("Student not found")

        // Check if a grade already exists
        val existingGrade = gradeRepository.getByAssignmentAndStudent(submission.assignmentId, submission.studentId)

        // Create or update grade
        val grade = if (existingGrade != null) {
            // Update existing grade
            val updatedGrade = existingGrade.copy(
                score = request.score,
                feedback = request.feedback,
                gradedAt = Instant.now()
            )

            if (!gradeRepository.update(updatedGrade)) {
                throw Exception("Failed to update grade")
            }

            updatedGrade
        } else {
            // Create new grade
            val newGrade = Grade(
                id = UUID.randomUUID(),
                studentId = submission.studentId,
                assignmentId = submission.assignmentId,
                score = request.score,
                feedback = request.feedback,
                gradedAt = Instant.now()
            )

            gradeRepository.create(newGrade)
        }

        logger.info { "Graded submission $submissionId with score ${grade.score}" }

        return GradeResponseDto.fromGrade(
            grade = grade,
            studentName = student.name
        )
    }

    /**
     * Get a grade by ID
     */
    override suspend fun getGradeById(id: UUID): GradeResponseDto {
        val grade = gradeRepository.getById(id)
            ?: throw NotFoundException("Grade not found")

        val student = userRepository.getById(grade.studentId)
            ?: throw NotFoundException("Student not found")

        return GradeResponseDto.fromGrade(
            grade = grade,
            studentName = student.name
        )
    }

    /**
     * Get all grades for an assignment
     */
    override suspend fun getGradesByAssignment(assignmentId: UUID): List<GradeResponseDto> {
        val assignment = assignmentRepository.getById(assignmentId)
            ?: throw NotFoundException("Assignment not found")

        val grades = gradeRepository.getByAssignment(assignmentId)

        return grades.mapNotNull { grade ->
            val student = userRepository.getById(grade.studentId) ?: return@mapNotNull null

            GradeResponseDto.fromGrade(
                grade = grade,
                studentName = student.name
            )
        }
    }

    /**
     * Get all grades for a student
     */
    override suspend fun getGradesByStudent(studentId: UUID): List<GradeResponseDto> {
        val student = userRepository.getById(studentId)
            ?: throw NotFoundException("Student not found")

        val grades = gradeRepository.getByStudent(studentId)

        return grades.map { grade ->
            GradeResponseDto.fromGrade(
                grade = grade,
                studentName = student.name
            )
        }
    }
}