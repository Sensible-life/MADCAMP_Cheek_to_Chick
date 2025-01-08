package com.google.ar.core.examples.kotlin.helloar.login

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.ar.core.examples.kotlin.helloar.MainActivity
import com.google.ar.core.examples.kotlin.helloar.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.mpackage.network.ApiService
import com.mpackage.network.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {



    private val TAG = "KaKaoLogin"

    override fun onCreate(savedInstanceState: Bundle?) {

        // logout 함수
        // logoutUser(this)


        val splashScreen = installSplashScreen()
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)    //자동 생성 상단바 없앰

        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_login)

        // 구글 로그인 버튼 클릭 리스너
        val googleLoginButton: CardView = findViewById(R.id.googleLoginButton)
        googleLoginButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 카카오 로그인 버튼 클릭 리스너
        val kakaoLoginButton: CardView = findViewById(R.id.kakaoLoginButton)
        kakaoLoginButton.setOnClickListener {

            // 카카오톡으로 로그인 시도
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    startKakaoLogin(token, error)
                }
            } else {
                // 카카오계정으로 로그인 시도
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    startKakaoLogin(token, error)
                }
            }

        }
    }

    private fun startKakaoLogin(token: OAuthToken?, error: Throwable?) {
        // 카카오 계정으로 로그인 콜백
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오 로그인 실패", error)
                Toast.makeText(this, "카카오 로그인 실패: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.i(TAG, "카카오 로그인 성공 ${token.accessToken}")
                Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show()

                // 성공 후 메인 액티비티로 이동
                startActivity(Intent(this, MainActivity::class.java))

                // 서버로 Access Token 전달
                sendAccessTokenToServer(token.accessToken)
            }
        }

        // 카카오톡이 설치되어 있는 경우
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            // 카카오톡이 설치되지 않은 경우 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    // 인가 코드를 서버로 전달
    private fun sendAccessTokenToServer(accessToken: String) {

        // Retrofit 객체 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://143.248.225.199:3000") // 서버의 실제 IP 주소로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // OkHttpClient Interceptor 로 네트워크 요청 디버깅
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // 비동기로 서버에 요청
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.sendAccessToken(accessToken)
                if (response.isSuccessful) {
                    // 서버 응답 성공
                    val userProfile = response.body() // 서버 응답 데이터

                    userProfile?.let {
                        saveUserProfileToSharedPreferences(it) // SharedPreferences에 저장
                    }
                    // 서버 응답 성공
                    Log.i("AccessToken", "서버 응답 성공: ${response.body()}")
                } else {
                    // 서버 응답 실패
                    Log.e("AccessToken", "서버 응답 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("AccessToken", "서버 요청 중 오류 발생", e)
            }
        }

    }

    fun logoutUser(context: Context) {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("Logout", "카카오 로그아웃 실패", error)
                Toast.makeText(context, "로그아웃 실패: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            } else {
                Log.i("Logout", "카카오 로그아웃 성공")
                Toast.makeText(context, "로그아웃 성공", Toast.LENGTH_SHORT).show()

                // SharedPreferences 초기화
                clearProfileData()

                // 로그인 화면으로 이동
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }

    private fun saveUserProfileToSharedPreferences(userProfile: UserProfile) {


        val sharedPreferences = getSharedPreferences("ProfileSharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("username", userProfile.username)
        editor.putString("email", userProfile.email)
        editor.putString("profileImage", userProfile.profileImage)
        editor.apply() // 저장*/

        // 저장된 값 확인 로그 추가
        Log.d(TAG, "SharedPreferences saved username: ${userProfile.username}")
        Log.d(TAG, "SharedPreferences saved email: ${userProfile.email}")
        Log.d(TAG, "SharedPreferences saved profileImage: ${userProfile.profileImage}")
    }


    private fun clearProfileData() {
        val sharedPreferences = getSharedPreferences("ProfileSharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.remove("username")
        editor.remove("email")
        editor.remove("profileImage")
        editor.apply()

    }
}

