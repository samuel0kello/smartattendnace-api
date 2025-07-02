package com.example.features.courses.repositories

import com.example.di.dbQuery
import com.example.domain.models.CourseSchedule
import com.example.domain.tables.CourseSchedules
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CourseScheduleRepository
 */
@Singleton
class CourseScheduleRepositoryImpl @Inject constructor() : CourseScheduleRepository {
    
    override suspend fun getByCourseId(courseId: UUID): List<CourseSchedule> = dbQuery {
        CourseSchedules.select { CourseSchedules.courseId eq courseId }
            .map { it.toCourseSchedule() }
    }
    
    override suspend fun getById(id: UUID): CourseSchedule? = dbQuery {
        CourseSchedules.select { CourseSchedules.id eq id }
            .map { it.toCourseSchedule() }
            .singleOrNull()
    }
    
    override suspend fun create(schedule: CourseSchedule): CourseSchedule = dbQuery {
        val insertStatement = CourseSchedules.insert {
            it[id] = schedule.id
            it[courseId] = schedule.courseId
            it[dayOfWeek] = schedule.dayOfWeek
            it[startTime] = schedule.startTime
            it[endTime] = schedule.endTime
            it[roomNumber] = schedule.roomNumber
            it[meetingLink] = schedule.meetingLink
        }
        
        insertStatement.resultedValues?.singleOrNull()?.toCourseSchedule()
            ?: throw Exception("Failed to insert course schedule")
    }
    
    override suspend fun update(schedule: CourseSchedule): Boolean = dbQuery {
        val updatedRows = CourseSchedules.update({ CourseSchedules.id eq schedule.id }) {
            it[dayOfWeek] = schedule.dayOfWeek
            it[startTime] = schedule.startTime
            it[endTime] = schedule.endTime
            it[roomNumber] = schedule.roomNumber
            it[meetingLink] = schedule.meetingLink
        }
        updatedRows > 0
    }
    
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val deletedRows = CourseSchedules.deleteWhere { CourseSchedules.id eq id }
        deletedRows > 0
    }
    
    override suspend fun deleteByCourseId(courseId: UUID): Boolean = dbQuery {
        val deletedRows = CourseSchedules.deleteWhere { CourseSchedules.courseId eq courseId }
        deletedRows > 0
    }
    
    /**
     * Convert ResultRow to CourseSchedule model
     */
    private fun ResultRow.toCourseSchedule(): CourseSchedule = CourseSchedule(
        id = this[CourseSchedules.id].value,
        courseId = this[CourseSchedules.courseId].value,
        dayOfWeek = this[CourseSchedules.dayOfWeek],
        startTime = this[CourseSchedules.startTime],
        endTime = this[CourseSchedules.endTime],
        roomNumber = this[CourseSchedules.roomNumber],
        meetingLink = this[CourseSchedules.meetingLink]
    )
}