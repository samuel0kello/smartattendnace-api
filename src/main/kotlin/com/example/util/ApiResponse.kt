package com.example.util

import com.example.model.ResponseData
import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    @Serializable(with = ResponseDataSerializer::class)
    val data: ResponseData? = null,
    val error: String? = null
)
