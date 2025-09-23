package com.example.infrastructure.database.entity


import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime


object AuditLogTable : IntIdTable("audit_log_table") {
    val actor = reference("actor_id", UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val actorRole = varchar("actor_role", 20).check {
        it inList listOf("Student", "Lecturer", "Admin")
    }.nullable()
    val action = varchar("action", 50)
    val targetTable = varchar("target_table", 50).nullable()
    val targetId = integer("target_id").nullable()
    @OptIn(ExperimentalTime::class)
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}

class AuditLogEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuditLogEntity>(AuditLogTable)

    var actorId by AuditLogTable.actor
    var actorRole by AuditLogTable.actorRole
    var action by AuditLogTable.action
    var targetTable by AuditLogTable.targetTable
    var targetId by AuditLogTable.targetId
    @OptIn(ExperimentalTime::class)
    var timestamp by AuditLogTable.timestamp
}
