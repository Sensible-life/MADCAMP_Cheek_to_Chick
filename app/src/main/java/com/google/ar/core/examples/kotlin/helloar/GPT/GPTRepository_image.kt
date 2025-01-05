package com.google.ar.core.examples.kotlin.helloar.GPT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GPTRepository_image {
    private val api = RetrofitClient.instance_image

    suspend fun generateImage(prompt: String): List<String> {
        return try {
            val request = DalleRequest(prompt = prompt, n = 1, size = "256x256")
            val response = api.generateImage(request)
            response.data.map { it.url } // DalleResponse에서 이미지 URL 리스트 추출
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // 오류 발생 시 빈 리스트 반환
        }
    }
}

