package com.example.infrastructure.database.entity

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

object Attendance : IntIdTable("Attendance") {
    val enrollment = reference("enrollment_id", Enrollments, onDelete = ReferenceOption.CASCADE)
    val schedule = reference("schedule_id", Schedules, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20).check {
        it inList listOf("Present", "Absent", "Late", "Excused")
    }
    val date = date("date")
    @OptIn(ExperimentalTime::class) val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class) val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class AttendanceRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AttendanceRecord>(Attendance)

    var enrollment by Enrollment referencedOn Attendance.enrollment
    var schedule by Schedule referencedOn Attendance.schedule
    var status by Attendance.status
    var date by Attendance.date
    @OptIn(ExperimentalTime::class) var createdAt by Attendance.createdAt
    @OptIn(ExperimentalTime::class) var updatedAt by Attendance.updatedAt
}