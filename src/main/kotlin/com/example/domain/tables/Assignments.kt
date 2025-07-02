package com.example.domain.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Assignments table definition
 */
object Assignments : UUIDTable("assignments") {
    val courseId = reference("course_id", Courses.id)
    val lecturerId = reference("lecturer_id", Users.id)
    val title = varchar("title", 255)
    val description = text("description")
    val dueDate = timestamp("due_date")
    val createdAt = timestamp("created_at")
}

/**
 * Submissions table definition
 */
object Submissions : UUIDTable("submissions") {
    val studentId = reference("student_id", Users.id)
    val assignmentId = reference("assignment_id", Assignments.id)
    val fileUrl = text("file_url")
    val submissionDate = timestamp("submission_date")
    val grade = float("grade").nullable()
}

/**
 * Grades table definition
 */
object Grades : UUIDTable("grades") {
    val studentId = reference("student_id", Users.id)
    val assignmentId = reference("assignment_id", Assignments.id)
    val score = float("score")
    val feedback = text("feedback").nullable()
    val gradedAt = timestamp("graded_at")
}