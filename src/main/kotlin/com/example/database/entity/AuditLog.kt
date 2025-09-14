package com.example.database.entity


import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.time
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object AuditLog : IntIdTable("AuditLog") {
    val actorId = reference("actor_id", Users.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val actorRole = varchar("actor_role", 20).check {
        it inList listOf("Student", "Lecturer", "Admin")
    }.nullable()
    val action = varchar("action", 50)
    val targetTable = varchar("target_table", 50).nullable()
    val targetId = integer("target_id").nullable()
    @OptIn(ExperimentalTime::class)
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}
