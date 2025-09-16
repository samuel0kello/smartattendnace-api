package com.example.shared

import com.example.domain.model.AccessTokenResponse
import com.example.domain.model.CredentialsResponse
import com.example.domain.model.LoginTokenResponse
import com.example.domain.model.PasswordResetResponse
import com.example.domain.model.ResponseData
import com.example.domain.model.UserResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ResponseData::class)
object ResponseDataSerializer : KSerializer<ResponseData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ResponseData")

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: ResponseData) {
        when (value) {
            is AccessTokenResponse -> encoder.encodeSerializableValue(AccessTokenResponse.serializer(), value)
            is CredentialsResponse -> encoder.encodeSerializableValue(CredentialsResponse.serializer(), value)
            is LoginTokenResponse -> encoder.encodeSerializableValue(LoginTokenResponse.serializer(), value)
            is PasswordResetResponse -> encoder.encodeSerializableValue(PasswordResetResponse.serializer(), value)
            is UserResponse -> encoder.encodeSerializableValue(UserResponse.serializer(), value)
            else -> {}
        }
    }
    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): ResponseData {
        throw IllegalStateException("Deserialization is not supported for ResponseDataSerializer")
    }
}