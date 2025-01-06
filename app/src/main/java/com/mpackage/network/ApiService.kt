package com.mpackage.network


import android.telecom.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST




//AccessToken 서버로 전달
interface ApiService {
    @POST("/auth/kakao/token")
    @FormUrlEncoded
    suspend fun sendAccessToken(@Field("access_token") accessToken: String): Response<UserProfile>

}


