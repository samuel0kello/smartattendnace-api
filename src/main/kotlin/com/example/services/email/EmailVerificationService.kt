package com.example.services.email

import com.example.config.AppConfig
import com.example.database.entity.User
import java.time.LocalDateTime
import java.util.UUID

class EmailVerificationService(
    private val emailService: EmailService,
    private val appConfig: AppConfig
) {
    companion object {
        private const val TOKEN_VALIDITY_HOURS = 24
    }

    fun sendVerificationEmail(user: User) {
        val verificationToken = generateVerificationToken()
        val verificationLink = "${appConfig.deployment.baseUrl}/api/auth/verify-email?token=$verificationToken"
        
        user.apply {
            emailVerificationToken = verificationToken
            emailVerificationTokenExpiry = LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS.toLong())
        }

        val emailContent = """
            <html>
                <body>
                    <h1>Welcome to Smart Attendance!</h1>
                    <p>Dear ${user.firstName},</p>
                    <p>Thank you for registering. Please verify your email address by clicking the link below:</p>
                    <p><a href="$verificationLink">Verify Email Address</a></p>
                    <p>This link will expire in 24 hours.</p>
                    <p>If you didn't create an account, please ignore this email.</p>
                </body>
            </html>
        """.trimIndent()

        emailService.sendEmail(
            to = user.email,
            subject = "Verify Your Email Address",
            content = emailContent,
            isHtml = true
        )
    }

    private fun generateVerificationToken(): String = UUID.randomUUID().toString()
}
