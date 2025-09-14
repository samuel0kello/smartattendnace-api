package com.example.database.entity

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

object Attendance : IntIdTable("Attendance") {
    val enrollmentId = reference("enrollment_id", Enrollment.id, onDelete = ReferenceOption.CASCADE)
    val scheduleId = reference("schedule_id", Schedule.id, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20).check {
        it inList listOf("Present", "Absent", "Late", "Excused")
    }
    val date = date("date")
    @OptIn(ExperimentalTime::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
