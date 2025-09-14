package com.example.database.entity

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.time
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object Schedule : IntIdTable("Schedule") {
    val courseId = reference("course_id", Course.id, onDelete = ReferenceOption.CASCADE)
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
