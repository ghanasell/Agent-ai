package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeepSeekMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class DeepSeekRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<DeepSeekMessage>,
    @Json(name = "temperature") val temperature: Double = 0.2,
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    @Json(name = "stream") val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DeepSeekChoice(
    @Json(name = "index") val index: Int,
    @Json(name = "message") val message: DeepSeekMessage,
    @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class DeepSeekUsage(
    @Json(name = "prompt_tokens") val promptTokens: Int,
    @Json(name = "completion_tokens") val completionTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int
)

@JsonClass(generateAdapter = true)
data class DeepSeekResponse(
    @Json(name = "id") val id: String,
    @Json(name = "object") val obj: String?,
    @Json(name = "created") val created: Long?,
    @Json(name = "model") val model: String,
    @Json(name = "choices") val choices: List<DeepSeekChoice>,
    @Json(name = "usage") val usage: DeepSeekUsage?
)
