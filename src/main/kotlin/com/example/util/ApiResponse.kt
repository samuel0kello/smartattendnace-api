package com.example.util

import com.example.model.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor


@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    @Serializable(with = ResponseDataSerializer::class)
    val data: ResponseData? = null,
    val error: String? = null
)
