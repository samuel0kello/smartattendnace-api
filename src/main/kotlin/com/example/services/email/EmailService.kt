package com.example.services.email

import com.example.config.SmtpConfig
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

class EmailService(private val config: SmtpConfig) {
    private val session: Session by lazy { createSession() }

    private fun createSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            put("mail.smtp.ssl.enable", config.ssl.toString())
            put("mail.smtp.starttls.enable", config.starttls.toString())
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        })
    }

    fun sendEmail(to: String, subject: String, content: String, isHtml: Boolean = false) {
        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.fromEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                if (isHtml) {
                    setContent(content, "text/html; charset=utf-8")
                } else {
                    setText(content)
                }
            }
            Transport.send(message)
        } catch (e: MessagingException) {
            throw EmailServiceException("Failed to send email error ${e.message}", e)
        }
    }
}

class EmailServiceException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)