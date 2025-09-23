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

object AttendanceTable : IntIdTable("attendance") {
    val enrollment = reference("enrollment_id", Enrollments, onDelete = ReferenceOption.CASCADE)
    val schedule = reference("schedule_id", SchedulesTable, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20).check {
        it inList listOf("Present", "Absent", "Late", "Excused")
    }
    val date = date("date")
    @OptIn(ExperimentalTime::class) val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class) val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class AttendanceEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AttendanceEntity>(AttendanceTable)

    var enrollment by Enrollment referencedOn AttendanceTable.enrollment
    var schedule by Schedule referencedOn AttendanceTable.schedule
    var status by AttendanceTable.status
    var date by AttendanceTable.date
    @OptIn(ExperimentalTime::class) var createdAt by AttendanceTable.createdAt
    @OptIn(ExperimentalTime::class) var updatedAt by AttendanceTable.updatedAt
}