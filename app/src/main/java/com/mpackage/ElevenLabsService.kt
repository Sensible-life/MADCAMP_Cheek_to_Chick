package com.mpackage

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ElevenLabsService {
    @Multipart
    @POST("voices/add")
    fun addVoice(
        @Header("xi-api-key") apiKey: String,   // API 키
        @Part("name") name: String,            // name은 String으로 전달
        @Part file: MultipartBody.Part         // file은 MultipartBody.Part로 전달
    ): Call<ResponseBody>
}
