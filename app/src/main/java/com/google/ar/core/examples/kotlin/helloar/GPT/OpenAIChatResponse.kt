package com.google.ar.core.examples.kotlin.helloar.GPT

data class OpenAIChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val role: String,
    val content: String
)
