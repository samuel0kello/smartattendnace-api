package com.example.domain.services.email

import com.example.web.Routes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class EmailVerificationService(
    private val emailService: EmailService
) {
    fun sendVerificationEmail(toEmail: String, verificationToken: String, baseUrl: String) {
        val base = baseUrl.trimEnd('/')
        val encodedToken = URLEncoder.encode(verificationToken, StandardCharsets.UTF_8.toString())
        val verificationLink = "$base${Routes.VERIFY_EMAIL}?token=$encodedToken"

        val emailContent = """
            <html>
                <body>
                    <h1>Welcome to Smart Attendance!</h1>
                    <p>Dear user,</p>
                    <p>Please verify your email address by clicking the link below:</p>
                    <p><a href="$verificationLink">Verify Email Address</a></p>
                    <p>This link will expire in 24 hours.</p>
                </body>
            </html>
        """.trimIndent()

        emailService.sendEmail(
            to = toEmail,
            subject = "Verify Your Email Address",
            content = emailContent,
            isHtml = true
        )
    }
}