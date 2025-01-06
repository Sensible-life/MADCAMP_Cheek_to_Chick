package com.mpackage.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookSaveApi{
    @POST("/api/books")
    suspend fun sendBookData(@Body bookData: Book): Response<Any>
}