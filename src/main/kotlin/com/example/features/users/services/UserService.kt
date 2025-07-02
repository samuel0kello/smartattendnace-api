package com.example.features.users.services

import com.example.common.exceptions.BadRequestException
import com.example.common.exceptions.ConflictException
import com.example.common.exceptions.NotFoundException
import com.example.domain.models.Staff
import com.example.domain.models.Student
import com.example.domain.models.User
import com.example.domain.models.UserRole
import com.example.features.users.models.CreateUserRequest
import com.example.features.users.models.UpdateUserRequest
import com.example.features.users.models.UserResponseDto
import com.example.features.users.repositories.StaffRepository
import com.example.features.users.repositories.StudentRepository
import com.example.features.users.repositories.UserRepository
import mu.KotlinLogging
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

/**
 * User service interface
 */
interface UserService {
    suspend fun getUserById(id: UUID): UserResponseDto
    suspend fun getAllUsers(): List<UserResponseDto>
    suspend fun createUser(request: CreateUserRequest): UserResponseDto
    suspend fun updateUser(id: UUID, request: UpdateUserRequest): UserResponseDto
    suspend fun deleteUser(id: UUID): Boolean
}

/**
 * Implementation of UserService
 */
@Singleton
class UserServiceImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val staffRepository: StaffRepository
) : UserService {

    /**
     * Get a user by ID with role-specific details
     */
    override suspend fun getUserById(id: UUID): UserResponseDto {
        val user = userRepository.getById(id)
            ?: throw NotFoundException("User not found")

        // Fetch role-specific details
        return when (user.role) {
            UserRole.STUDENT -> {
                val student = studentRepository.findByUserId(id)
                UserResponseDto.fromUser(
                    user,
                    regNo = student?.regNo,
                    department = student?.department,
                    yearOfStudy = student?.yearOfStudy
                )
            }
            UserRole.LECTURER, UserRole.ADMIN -> {
                val staff = staffRepository.findByUserId(id)
                UserResponseDto.fromUser(
                    user,
                    employeeId = staff?.employeeId,
                    department = staff?.department,
                    position = staff?.position
                )
            }
        }
    }

    /**
     * Get all users with their role-specific details
     */
    override suspend fun getAllUsers(): List<UserResponseDto> {
        val users = userRepository.getAll()

        return users.map { user ->
            when (user.role) {
                UserRole.STUDENT -> {
                    val student = studentRepository.findByUserId(user.id)
                    UserResponseDto.fromUser(
                        user,
                        regNo = student?.regNo,
                        department = student?.department,
                        yearOfStudy = student?.yearOfStudy
                    )
                }
                UserRole.LECTURER, UserRole.ADMIN -> {
                    val staff = staffRepository.findByUserId(user.id)
                    UserResponseDto.fromUser(
                        user,
                        employeeId = staff?.employeeId,
                        department = staff?.department,
                        position = staff?.position
                    )
                }
            }
        }
    }

    /**
     * Create a new user with role-specific details
     */
    override suspend fun createUser(request: CreateUserRequest): UserResponseDto {
        logger.info { "Creating user with email ${request.email} and role ${request.role}" }

        // Validate request
        if (request.name.isBlank() || request.email.isBlank() || request.password.isBlank() || request.role.isBlank()) {
            throw BadRequestException("Name, email, password, and role are required")
        }

        // Check if email already exists
        if (userRepository.getByEmail(request.email) != null) {
            throw ConflictException("A user with this email already exists")
        }

        // Parse role
        val role = try {
            UserRole.valueOf(request.role.uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid role: ${request.role}")
        }

        // Create user
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = User(
            id = UUID.randomUUID(),
            name = request.name,
            email = request.email,
            password = hashedPassword,
            role = role,
            createdAt = Instant.now()
        )

        val createdUser = userRepository.create(user)
        logger.info { "Created user with ID: ${createdUser.id}" }

        // Create role-specific records
        when (role) {
            UserRole.STUDENT -> {
                if (request.regNo.isNullOrBlank()) {
                    throw BadRequestException("Registration number is required for students")
                }

                // Check if registration number already exists
                if (studentRepository.findByRegNo(request.regNo) != null) {
                    userRepository.delete(createdUser.id) // Rollback
                    throw ConflictException("A student with this registration number already exists")
                }

                val student = Student(
                    userId = createdUser.id,
                    regNo = request.regNo,
                    department = request.department,
                    yearOfStudy = request.yearOfStudy
                )
                studentRepository.create(student)

                return UserResponseDto.fromUser(
                    createdUser,
                    regNo = student.regNo,
                    department = student.department,
                    yearOfStudy = student.yearOfStudy
                )
            }
            UserRole.LECTURER -> {
                if (request.employeeId.isNullOrBlank()) {
                    throw BadRequestException("Employee ID is required for lecturers")
                }

                // Check if employee ID already exists
                if (staffRepository.findByEmployeeId(request.employeeId) != null) {
                    userRepository.delete(createdUser.id) // Rollback
                    throw ConflictException("A staff member with this employee ID already exists")
                }

                val staff = Staff(
                    userId = createdUser.id,
                    employeeId = request.employeeId,
                    department = request.department,
                    position = "Lecturer"
                )
                staffRepository.create(staff)

                return UserResponseDto.fromUser(
                    createdUser,
                    employeeId = staff.employeeId,
                    department = staff.department,
                    position = staff.position
                )
            }
            UserRole.ADMIN -> {
                // Generate employee ID for admin if not provided
                val employeeId = request.employeeId ?: "ADM-" + UUID.randomUUID().toString().substring(0, 8)

                val staff = Staff(
                    userId = createdUser.id,
                    employeeId = employeeId,
                    department = request.department,
                    position = "Administrator"
                )
                staffRepository.create(staff)

                return UserResponseDto.fromUser(
                    createdUser,
                    employeeId = staff.employeeId,
                    department = staff.department,
                    position = staff.position
                )
            }
        }
    }

    /**
     * Update a user's information
     */
    override suspend fun updateUser(id: UUID, request: UpdateUserRequest): UserResponseDto {
        logger.info { "Updating user with ID $id" }

        // Get existing user
        val existingUser = userRepository.getById(id)
            ?: throw NotFoundException("User not found")

        // Check if email is being changed and already exists
        if (request.email != null && request.email != existingUser.email) {
            if (userRepository.getByEmail(request.email) != null) {
                throw ConflictException("A user with this email already exists")
            }
        }

        // Update basic user info
        val updatedUser = existingUser.copy(
            name = request.name ?: existingUser.name,
            email = request.email ?: existingUser.email,
            password = if (request.password != null) BCrypt.hashpw(request.password, BCrypt.gensalt()) else existingUser.password,
            updatedAt = Instant.now()
        )

        // Update in database
        if (!userRepository.update(updatedUser)) {
            throw Exception("Failed to update user")
        }

        // Update role-specific info
        when (existingUser.role) {
            UserRole.STUDENT -> {
                val student = studentRepository.findByUserId(id)
                    ?: throw NotFoundException("Student record not found")

                val updatedStudent = student.copy(
                    department = request.department ?: student.department,
                    yearOfStudy = request.yearOfStudy ?: student.yearOfStudy
                )

                studentRepository.update(updatedStudent)

                return UserResponseDto.fromUser(
                    updatedUser,
                    regNo = updatedStudent.regNo,
                    department = updatedStudent.department,
                    yearOfStudy = updatedStudent.yearOfStudy
                )
            }
            UserRole.LECTURER, UserRole.ADMIN -> {
                val staff = staffRepository.findByUserId(id)
                    ?: throw NotFoundException("Staff record not found")

                val updatedStaff = staff.copy(
                    department = request.department ?: staff.department
                )

                staffRepository.update(updatedStaff)

                return UserResponseDto.fromUser(
                    updatedUser,
                    employeeId = updatedStaff.employeeId,
                    department = updatedStaff.department,
                    position = updatedStaff.position
                )
            }
        }
    }

    /**
     * Delete a user and their role-specific records
     */
    override suspend fun deleteUser(id: UUID): Boolean {
        logger.info { "Deleting user with ID $id" }

        // Get existing user
        val existingUser = userRepository.getById(id)
            ?: throw NotFoundException("User not found")

        // Delete role-specific records first
        when (existingUser.role) {
            UserRole.STUDENT -> {
                studentRepository.delete(id)
            }
            UserRole.LECTURER, UserRole.ADMIN -> {
                staffRepository.delete(id)
            }
        }

        // Delete the user
        return userRepository.delete(id)
    }
}
