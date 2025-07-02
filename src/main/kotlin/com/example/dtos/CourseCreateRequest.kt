package com.example.dtos

import com.example.domain.models.SessionType
import kotlinx.serialization.Serializable

// Course DTOs
@Serializable
data class CourseCreateRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CourseUpdateRequest(
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class CourseResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val lecturerId: String,
    val lecturerName: String,
    val createdAt: String,
    val studentsCount: Int = 0
)

// Attendance DTOs
@Serializable
data class AttendanceSessionCreateRequest(
    val courseId: String,
    val durationMinutes: Int,
    val sessionType: SessionType,
    val geoFence: GeoFenceDetails? = null
)

@Serializable
data class GeoFenceDetails(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double
)

@Serializable
data class AttendanceSessionResponse(
    val id: String,
    val courseId: String,
    val sessionCode: String,
    val sessionType: SessionType,
    val createdAt: String,
    val expiresAt: String,
    val geoFence: GeoFenceDetails? = null
)

@Serializable
data class AttendanceMarkRequest(
    val sessionCode: String,
    val location: LocationDetails? = null
)

@Serializable
data class LocationDetails(
    val latitude: Double,
    val longitude: Double
)