package com.mpackage

import com.google.ar.core.examples.kotlin.helloar.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ElevenLabRetrofit {

    private const val BASE_URL = "https://api.elevenlabs.io/v1/"

    val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("xi-api-key", BuildConfig.ELEVEN_API_KEY) // API Key를 직접 추가
                    .addHeader("Accept", "*/*") // 명시적으로 Accept 헤더 추가
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
