package com.example.database.entity


import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.time
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object Enrollment : IntIdTable("Enrollment") {
    val studentId = reference("student_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val courseId = reference("course_id", Course.id, onDelete = ReferenceOption.CASCADE)
    @OptIn(ExperimentalTime::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
