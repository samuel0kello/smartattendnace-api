package com.example.di

import com.example.features.assignments.repositories.AssignmentRepositoryImpl
import com.example.features.assignments.repositories.GradeRepositoryImpl
import com.example.features.assignments.repositories.SubmissionRepositoryImpl
import com.example.features.assignments.services.AssignmentService
import com.example.features.assignments.services.AssignmentServiceImpl
import com.example.features.attendance.repositories.AttendanceRepositoryImpl
import com.example.features.attendance.repositories.AttendanceSessionRepositoryImpl
import com.example.features.attendance.services.AttendanceService
import com.example.features.attendance.services.AttendanceServiceImpl
import com.example.features.auth.repositories.AuthRepositoryImpl
import com.example.features.auth.services.AuthService
import com.example.features.auth.services.AuthServiceImpl
import com.example.features.auth.services.JwtService
import com.example.features.auth.util.RoleAuthorization
import com.example.features.auth.util.RoleAuthorizationImpl
import com.example.features.courses.repositories.CourseRepositoryImpl
import com.example.features.courses.repositories.CourseScheduleRepositoryImpl
import com.example.features.courses.services.CourseService
import com.example.features.courses.services.CourseServiceImpl
import com.example.features.users.repositories.StaffRepositoryImpl
import com.example.features.users.repositories.StudentRepositoryImpl
import com.example.features.users.repositories.UserRepositoryImpl
import com.example.features.users.services.UserService
import com.example.features.users.services.UserServiceImpl
import javax.inject.Singleton

/**
 * Manual implementation of ApplicationComponent
 */
@Singleton
class ApplicationComponentImpl private constructor(
    private val databaseFactory: DatabaseFactory,
    private val jwtService: JwtService,
    private val roleAuthorization: RoleAuthorization,
    private val userService: UserService,
    private val authService: AuthService,
    private val courseService: CourseService,
    private val attendanceService: AttendanceService,
    private val assignmentService: AssignmentService
) : ApplicationComponent {

    override fun databaseFactory(): DatabaseFactory = databaseFactory
    override fun jwtService(): JwtService = jwtService
    override fun roleAuthorization(): RoleAuthorization = roleAuthorization
    override fun userService(): UserService = userService
    override fun authService(): AuthService = authService
    override fun courseService(): CourseService = courseService
    override fun attendanceService(): AttendanceService = attendanceService
    override fun assignmentService(): AssignmentService = assignmentService

    /**
     * Builder for ApplicationComponentImpl
     */
    class Builder {
        fun build(): ApplicationComponent {
            // Create all dependencies manually
            val databaseFactory = DatabaseFactory()
            val jwtService = JwtService()
            val roleAuthorization = RoleAuthorizationImpl()

            // Create repositories
            val userRepository = UserRepositoryImpl()
            val studentRepository = StudentRepositoryImpl()
            val staffRepository = StaffRepositoryImpl()
            val authRepository = AuthRepositoryImpl()
            val courseRepository = CourseRepositoryImpl()
            val courseScheduleRepository = CourseScheduleRepositoryImpl()
            val attendanceSessionRepository = AttendanceSessionRepositoryImpl()
            val attendanceRepository = AttendanceRepositoryImpl()
            val assignmentRepository = AssignmentRepositoryImpl()
            val submissionRepository = SubmissionRepositoryImpl()
            val gradeRepository = GradeRepositoryImpl()

            // Create services
            val userService = UserServiceImpl(userRepository, studentRepository, staffRepository)
            val authService = AuthServiceImpl(authRepository, userRepository, studentRepository, staffRepository, jwtService)
            val courseService = CourseServiceImpl(courseRepository, courseScheduleRepository, userRepository)
            val attendanceService = AttendanceServiceImpl(attendanceSessionRepository, attendanceRepository, courseRepository, userRepository)
            val assignmentService = AssignmentServiceImpl(assignmentRepository, submissionRepository, gradeRepository, courseRepository, userRepository)

            return ApplicationComponentImpl(
                databaseFactory,
                jwtService,
                roleAuthorization,
                userService,
                authService,
                courseService,
                attendanceService,
                assignmentService
            )
        }
    }
}