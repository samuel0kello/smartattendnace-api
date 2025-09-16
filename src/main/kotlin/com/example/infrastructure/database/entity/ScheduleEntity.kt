package com.example.infrastructure.database.entity

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.time
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object Schedules : IntIdTable("Schedule") {
    val course = reference("course_id", Courses, onDelete = ReferenceOption.CASCADE)
    val dayOfWeek = varchar("day_of_week", 10).check {
        it inList listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
    val startTime = time("start_time")
    val endTime = time("end_time")
    val frequency = varchar("frequency", 50).nullable()
    @OptIn(ExperimentalTime::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class Schedule(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Schedule>(Schedules)

    var course by Course referencedOn Schedules.course
    var dayOfWeek by Schedules.dayOfWeek
    var startTime by Schedules.startTime
    var endTime by Schedules.endTime
    var frequency by Schedules.frequency
    @OptIn(ExperimentalTime::class) var createdAt by Schedules.createdAt
    @OptIn(ExperimentalTime::class)var updatedAt by Schedules.updatedAt
}
