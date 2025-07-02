package com.example.domain.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Lectures table definition
 */
object Lectures : UUIDTable("lectures") {
    val courseId = reference("course_id", Courses.id)
    val lecturerId = reference("lecturer_id", Users.id)
    val topic = varchar("topic", 255)
    val date = timestamp("date")
    val duration = integer("duration") // Duration in minutes
}
