package com.example.util

import com.example.model.ResponseData
import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ResponseData? = null,
    val error: String? = null
)