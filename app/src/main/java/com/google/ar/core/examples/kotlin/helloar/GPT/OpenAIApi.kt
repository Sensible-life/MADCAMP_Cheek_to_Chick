package com.google.ar.core.examples.kotlin.helloar.GPT

import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: OpenAIChatRequest): OpenAIChatResponse
}
