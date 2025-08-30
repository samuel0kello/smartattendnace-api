package com.example.web

object Routes {
    const val AUTH_PREFIX = "/auth"

    const val SIGN_UP = "$AUTH_PREFIX/sign-up"
    const val SIGN_IN = "$AUTH_PREFIX/sign-in"
    const val VERIFY_EMAIL = "$AUTH_PREFIX/verify-email"
    const val REFRESH = "$AUTH_PREFIX/refresh"
    const val FORGOT_PASSWORD = "$AUTH_PREFIX/forgot_password"
    const val CHANGE_PASSWORD = "$AUTH_PREFIX/change_password"
}