package com.google.ar.core.examples.kotlin.helloar.GPT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GPTRepository {
    private val api = RetrofitClient.instance

    suspend fun getChatResponse(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val request = OpenAIChatRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(
                        RequestMessage(role = "system", content = "You are a helpful assistant."),
                        RequestMessage(role = "user", content = userMessage)
                    ),
                    max_tokens = 400,
                    temperature = 0.7
                )
                val response = api.getChatCompletion(request)
                response.choices[0].message.content.trim()
            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }
    }
}
