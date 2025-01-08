package com.mpackage.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ListedBooksApi {
    @GET("/api/books/all") // 모든 도서 데이터를 가져오는 API
    suspend fun getAllBooks(): Response<List<LikedBooks>>

    @POST("/api/books/updateStatus")
    suspend fun updateBookStatus(
        @Field("title") title: String, // 책 제목
        @Query("like") isLiked: Boolean
    ): Response<Unit>
}
