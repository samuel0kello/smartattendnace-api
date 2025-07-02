package com.example.di

import dagger.Component
import com.example.features.assignments.services.AssignmentService
import com.example.features.attendance.services.AttendanceService
import com.example.features.auth.services.AuthService
import com.example.features.auth.services.JwtService
import com.example.features.auth.util.RoleAuthorization
import com.example.features.courses.services.CourseService
import com.example.features.users.services.UserService
import javax.inject.Singleton

/**
 * Main application component for dependency injection
 */
@Singleton
@Component(modules = [
    AppModule::class,
    AuthModule::class,
    UserModule::class,
    CourseModule::class,
    AttendanceModule::class,
    AssignmentModule::class
])
interface ApplicationComponent {
    // Core services
    fun databaseFactory(): DatabaseFactory

    // Auth and security services
    fun jwtService(): JwtService
    fun authService(): AuthService
    fun roleAuthorization(): RoleAuthorization

    // Feature services
    fun userService(): UserService
    fun courseService(): CourseService
    fun attendanceService(): AttendanceService
    fun assignmentService(): AssignmentService

    @Component.Builder
    interface Builder {
        fun build(): ApplicationComponent
    }
}