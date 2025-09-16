package com.example.infrastructure.database.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object Courses : IntIdTable("Courses"){
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val credits = integer("credits")
    val lecturerId = reference("lecturer_id", Users.id)
    @OptIn(ExperimentalTime::class)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    @OptIn(ExperimentalTime::class)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class Course(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Course>(Courses)

    var title by Courses.title
    var description by Courses.description
    var credits by Courses.credits
    var lecturer by User referencedOn Courses.lecturerId

    @OptIn(ExperimentalTime::class)
    var createdAt by Courses.createdAt
    @OptIn(ExperimentalTime::class)
    var updatedAt by Courses.updatedAt
}
