package com.example.di

import com.example.features.assignments.services.AssignmentService
import com.example.features.attendance.services.AttendanceService
import com.example.features.auth.services.AuthService
import com.example.features.auth.services.JwtService
import com.example.features.auth.util.RoleAuthorization
import com.example.features.courses.services.CourseService
import com.example.features.users.services.UserService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Singleton injector for the application
 */
object AppInjector {
    init {
        logger.info { "Initializing application dependencies..." }
    }

    // We need to manually create the component
    private val component: ApplicationComponent by lazy {
        // Replace DaggerApplicationComponent with manual component creation
        ApplicationComponentImpl.Builder().build()
    }

    // Database
    val databaseFactory: DatabaseFactory by lazy {
        component.databaseFactory()
    }

    // Auth services
    val jwtService: JwtService by lazy { component.jwtService() }
    val authService: AuthService by lazy { component.authService() }
    val roleAuthorization: RoleAuthorization by lazy { component.roleAuthorization() }

    // Feature services
    val userService: UserService by lazy { component.userService() }
    val courseService: CourseService by lazy { component.courseService() }
    val attendanceService: AttendanceService by lazy { component.attendanceService() }
    val assignmentService: AssignmentService by lazy { component.assignmentService() }
}