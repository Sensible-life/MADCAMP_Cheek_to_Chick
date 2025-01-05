package com.google.ar.core.examples.kotlin.helloar.GPT

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DallEApi {
    @POST("v1/images/generations")
    suspend fun generateImage(@Body request: DalleRequest): DalleResponse
}
