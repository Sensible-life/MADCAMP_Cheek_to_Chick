package com.google.ar.core.examples.kotlin.helloar.GPT

data class OpenAIChatRequest(
    val model: String,
    val messages: List<RequestMessage>,
    val max_tokens: Int,
    val temperature: Double
)

data class RequestMessage(
    val role: String,
    val content: String
)
