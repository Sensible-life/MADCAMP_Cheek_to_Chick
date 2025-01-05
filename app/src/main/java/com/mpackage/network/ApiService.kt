package com.mpackage.network


import android.telecom.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class User(
    val id: String,
    val nickname: String,
    val profileImage: String?,
    val email: String
)



//AccessToken 서버로 전달
interface ApiService {
    @POST("/auth/kakao/token")
    @FormUrlEncoded
    suspend fun sendAccessToken(@Field("access_token") accessToken: String): Response<Any>

    @GET("users/{userId}")
    suspend fun getUser(@Path("nickname") nickname: String): User // suspend 함수로 변경
}


