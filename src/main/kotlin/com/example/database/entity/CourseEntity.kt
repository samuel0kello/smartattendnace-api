package com.example.database.entity

import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object Course : IntIdTable("Course"){
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val credits = integer("credits")
    val lecturerId = reference("lecturer_id", Users.id)
    @OptIn(ExperimentalTime::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
