package com.example.features.assignments.models

import com.example.domain.models.Assignment
import com.example.domain.models.Grade
import com.example.domain.models.Submission
import kotlinx.serialization.Serializable

/**
 * Assignment response DTO
 */
@Serializable
data class AssignmentResponseDto(
    val id: String,
    val courseId: String,
    val courseName: String,
    val lecturerId: String,
    val lecturerName: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val createdAt: String,
    val submissions: List<SubmissionResponseDto>? = null
) {
    companion object {
        fun fromAssignment(
            assignment: Assignment,
            courseName: String,
            lecturerName: String,
            submissions: List<SubmissionResponseDto>? = null
        ): AssignmentResponseDto {
            return AssignmentResponseDto(
                id = assignment.id.toString(),
                courseId = assignment.courseId.toString(),
                courseName = courseName,
                lecturerId = assignment.lecturerId.toString(),
                lecturerName = lecturerName,
                title = assignment.title,
                description = assignment.description,
                dueDate = assignment.dueDate.toString(),
                createdAt = assignment.createdAt.toString(),
                submissions = submissions
            )
        }
    }
}

/**
 * Create assignment request DTO
 */
@Serializable
data class CreateAssignmentRequest(
    val courseId: String,
    val title: String,
    val description: String,
    val dueDate: String
)

/**
 * Update assignment request DTO
 */
@Serializable
data class UpdateAssignmentRequest(
    val title: String? = null,
    val description: String? = null,
    val dueDate: String? = null
)

/**
 * Submission response DTO
 */
@Serializable
data class SubmissionResponseDto(
    val id: String,
    val assignmentId: String,
    val studentId: String,
    val studentName: String,
    val fileUrl: String,
    val submissionDate: String,
    val grade: Float? = null,
    val feedback: String? = null
) {
    companion object {
        fun fromSubmission(
            submission: Submission,
            studentName: String,
            grade: Grade? = null
        ): SubmissionResponseDto {
            return SubmissionResponseDto(
                id = submission.id.toString(),
                assignmentId = submission.assignmentId.toString(),
                studentId = submission.studentId.toString(),
                studentName = studentName,
                fileUrl = submission.fileUrl,
                submissionDate = submission.submissionDate.toString(),
                grade = grade?.score ?: submission.grade,
                feedback = grade?.feedback
            )
        }
    }
}

/**
 * Create submission request DTO
 */
@Serializable
data class CreateSubmissionRequest(
    val fileUrl: String
)

/**
 * Grade response DTO
 */
@Serializable
data class GradeResponseDto(
    val id: String,
    val assignmentId: String,
    val studentId: String,
    val studentName: String,
    val score: Float,
    val feedback: String? = null,
    val gradedAt: String
) {
    companion object {
        fun fromGrade(
            grade: Grade,
            studentName: String
        ): GradeResponseDto {
            return GradeResponseDto(
                id = grade.id.toString(),
                assignmentId = grade.assignmentId.toString(),
                studentId = grade.studentId.toString(),
                studentName = studentName,
                score = grade.score,
                feedback = grade.feedback,
                gradedAt = grade.gradedAt.toString()
            )
        }
    }
}

/**
 * Create or update grade request DTO
 */
@Serializable
data class GradeRequest(
    val score: Float,
    val feedback: String? = null
)
