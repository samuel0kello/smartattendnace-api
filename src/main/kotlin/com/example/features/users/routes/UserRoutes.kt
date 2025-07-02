package com.example.features.users.routes

import com.example.common.responses.success
import com.example.common.responses.successMessage
import com.example.di.AppInjector
import com.example.features.users.models.CreateUserRequest
import com.example.features.users.models.UpdateUserRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Configure user management routes
 */
fun Routing.configureUserRoutes() {
    val userService = AppInjector.userService
    val roleAuthorization = AppInjector.roleAuthorization
    
    route("/users") {
        // Public profile endpoint - no authentication required
        get("/profile/{id}") {
            val id = call.parameters["id"]?.let { 
                try {
                    UUID.fromString(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            } ?: throw IllegalArgumentException("Invalid user ID format")
            
            val user = userService.getUserById(id)
            call.respond(success(user))
        }
        
        // Protected endpoints
        authenticate("auth-jwt") {
            // Get current user profile
            get("/me") {
                val userId = roleAuthorization.getUserId(call)
                val user = userService.getUserById(UUID.fromString(userId))
                call.respond(success(user))
            }
            
            // Admin-only endpoints
            route("/admin") {
                // Get all users
                get {
                    roleAuthorization.requireAdmin(call)
                    
                    val users = userService.getAllUsers()
                    call.respond(success(users))
                }
                
                // Create a new user
                post {
                    roleAuthorization.requireAdmin(call)
                    
                    val request = call.receive<CreateUserRequest>()
                    val createdUser = userService.createUser(request)
                    
                    call.respond(HttpStatusCode.Created, success(createdUser))
                }
                
                // Update a user
                put("/{id}") {
                    roleAuthorization.requireAdmin(call)
                    
                    val id = call.parameters["id"]?.let { 
                        try {
                            UUID.fromString(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    } ?: throw IllegalArgumentException("Invalid user ID format")
                    
                    val request = call.receive<UpdateUserRequest>()
                    val updatedUser = userService.updateUser(id, request)
                    
                    call.respond(success(updatedUser))
                }
                
                // Delete a user
                delete("/{id}") {
                    roleAuthorization.requireAdmin(call)
                    
                    val id = call.parameters["id"]?.let { 
                        try {
                            UUID.fromString(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    } ?: throw IllegalArgumentException("Invalid user ID format")
                    
                    userService.deleteUser(id)
                    
                    call.respond(successMessage("User deleted successfully"))
                }
            }
            
            // User self-service endpoint
            put("/me") {
                val userId = roleAuthorization.getUserId(call)
                
                val request = call.receive<UpdateUserRequest>()
                val updatedUser = userService.updateUser(UUID.fromString(userId), request)
                
                call.respond(success(updatedUser))
            }
        }
    }
}