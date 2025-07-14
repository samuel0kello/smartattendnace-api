package com.example.domain.exception

class EnvLoadException(message: String, cause: Throwable? = null) :
    RuntimeException(
        message,
        cause
    )