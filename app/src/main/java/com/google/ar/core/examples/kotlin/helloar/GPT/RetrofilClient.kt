package com.google.ar.core.examples.kotlin.helloar.GPT

import android.content.Context
import android.util.Log
import com.google.ar.core.examples.kotlin.helloar.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import io.github.cdimascio.dotenv.dotenv
import java.io.File
import java.util.Properties


object RetrofitClient {
    private const val BASE_URL = "https://api.openai.com/"

    private val client by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val apiKey = BuildConfig.OPENAI_API_KEY
        // API 키를 헤더에 추가하는 인터셉터
        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }

        OkHttpClient.Builder()
            .addInterceptor(logging) // 로깅 인터셉터 추가
            .addInterceptor(apiKeyInterceptor) // API 키 인터셉터 추가
            .connectTimeout(200, TimeUnit.SECONDS) // 연결 타임아웃 설정
            .writeTimeout(200, TimeUnit.SECONDS) // 쓰기 타임아웃 설정
            .readTimeout(200, TimeUnit.SECONDS) // 읽기 타임아웃 설정
            .build()
    }

    val instance: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    val instance_image: DallEApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DallEApi::class.java)
    }
}
