package com.example.di

import com.example.features.assignments.repositories.*
import com.example.features.assignments.services.AssignmentService
import com.example.features.assignments.services.AssignmentServiceImpl
import com.example.features.attendance.repositories.AttendanceRepository
import com.example.features.attendance.repositories.AttendanceRepositoryImpl
import com.example.features.attendance.repositories.AttendanceSessionRepository
import com.example.features.attendance.repositories.AttendanceSessionRepositoryImpl
import com.example.features.attendance.services.AttendanceService
import com.example.features.attendance.services.AttendanceServiceImpl
import com.example.features.auth.repositories.AuthRepository
import com.example.features.auth.repositories.AuthRepositoryImpl
import com.example.features.auth.services.AuthService
import com.example.features.auth.services.AuthServiceImpl
import com.example.features.auth.services.JwtService
import com.example.features.courses.repositories.CourseRepository
import com.example.features.courses.repositories.CourseRepositoryImpl
import com.example.features.courses.repositories.CourseScheduleRepository
import com.example.features.courses.repositories.CourseScheduleRepositoryImpl
import com.example.features.courses.services.CourseService
import com.example.features.courses.services.CourseServiceImpl
import com.example.features.users.repositories.*
import com.example.features.users.services.UserService
import com.example.features.users.services.UserServiceImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Authentication module
 */
@Module
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAuthService(
        authRepository: AuthRepository,
        userRepository: UserRepository,
        studentRepository: StudentRepository,
        staffRepository: StaffRepository,
        jwtService: JwtService
    ): AuthService {
        return AuthServiceImpl(
            authRepository,
            userRepository,
            studentRepository,
            staffRepository,
            jwtService
        )
    }
}

/**
 * User management module
 */
@Module
object UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideStudentRepository(): StudentRepository {
        return StudentRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideStaffRepository(): StaffRepository {
        return StaffRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideUserService(
        userRepository: UserRepository,
        studentRepository: StudentRepository,
        staffRepository: StaffRepository
    ): UserService {
        return UserServiceImpl(userRepository, studentRepository, staffRepository)
    }
}

/**
 * Course management module
 */
@Module
object CourseModule {

    @Provides
    @Singleton
    fun provideCourseRepository(): CourseRepository {
        return CourseRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideCourseScheduleRepository(): CourseScheduleRepository {
        return CourseScheduleRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideCourseService(
        courseRepository: CourseRepository,
        courseScheduleRepository: CourseScheduleRepository,
        userRepository: UserRepository
    ): CourseService {
        return CourseServiceImpl(courseRepository, courseScheduleRepository,userRepository)
    }
}

/**
 * Attendance management module
 */
@Module
object AttendanceModule {

    @Provides
    @Singleton
    fun provideAttendanceRepository(): AttendanceRepository {
        return AttendanceRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAttendanceSessionRepository(): AttendanceSessionRepository {
        return AttendanceSessionRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAttendanceService(
        attendanceSessionRepository: AttendanceSessionRepository,
        attendanceRepository: AttendanceRepository,
        courseRepository: CourseRepository,
        userRepository: UserRepository
    ): AttendanceService {
        return AttendanceServiceImpl(attendanceSessionRepository, attendanceRepository, courseRepository, userRepository)
    }
}

/**
 * Assignment management module
 */
@Module
object AssignmentModule {

    @Provides
    @Singleton
    fun provideAssignmentRepository(): AssignmentRepository {
        return AssignmentRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSubmissionRepository(): SubmissionRepository {
        return SubmissionRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGradeRepository(): GradeRepository {
        return GradeRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAssignmentService(
        assignmentRepository: AssignmentRepository,
        courseRepository: CourseRepository,
        userRepository: UserRepository,
        submissionRepository: SubmissionRepository,
        gradeRepository: GradeRepository
    ): AssignmentService {
        return AssignmentServiceImpl(assignmentRepository, submissionRepository,gradeRepository,courseRepository, userRepository)
    }
}