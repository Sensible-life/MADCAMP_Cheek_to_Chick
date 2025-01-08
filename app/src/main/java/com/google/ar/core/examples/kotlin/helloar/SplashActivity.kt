package com.google.ar.core.examples.kotlin.helloar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.ar.core.examples.kotlin.helloar.HelloArRenderer.Companion.TAG
import com.google.ar.core.examples.kotlin.helloar.book.BookActivity
import com.google.ar.core.examples.kotlin.helloar.login.LoginActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk.keyHash
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
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)    //자동 생성 상단바 없앰
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loadingscreen_layout)
        // logoutUser(this)
        // 키 해시 얻기
        val DkeyHash = Utility.getKeyHash(this)
        Log.d("DKeyHash", "키 해시: $keyHash")
        // 릴리즈 키 해시 확인
        val RkeyHash = Utility.getKeyHash(this)
        Log.d("RKeyHash", "릴리즈 키 해시: $keyHash")

        // 애니메이션 비활성화
        overridePendingTransition(0, 0)

        var login: Boolean = false
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

        val splashGif = findViewById<ImageView>(R.id.splashGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // 디스크 캐싱 사용
                    .override(1800, 1800) // 원본 크기로 디코딩
                    .skipMemoryCache(true) // 메모리 캐시 비활성화
            )
            .into(splashGif)
    }


    private fun startKakaoLogin(token: OAuthToken?, error: Throwable?) {
        // 카카오 계정으로 로그인 콜백
        Log.d("DEBUG", "kakao login started")
        var loginSuccess = false

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오 로그인 실패", error)
                Toast.makeText(this, "카카오 로그인 실패: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }, 4000) // 3초 대기
            } else if (token != null) {
                Log.i(TAG, "카카오 로그인 성공 ${token.accessToken}")
                Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show()
                sendAccessTokenToServer(token.accessToken)
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }, 4000) // 3초 대기
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
    private fun saveUserProfileToSharedPreferences(userProfile: UserProfile) {

        val sharedPreferences = getSharedPreferences("ProfileSharedPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("username", userProfile.username)
        editor.putString("email", userProfile.email)
        editor.putString("profileImage", userProfile.profileImage)
        editor.apply() // 저장

        // 저장된 값 확인 로그 추가
        Log.d(TAG, "SharedPreferences saved username: ${userProfile.username}")
        Log.d(TAG, "SharedPreferences saved email: ${userProfile.email}")
        Log.d(TAG, "SharedPreferences saved profileImage: ${userProfile.profileImage}")
    }

    private fun proceedToNextActivity(login: Boolean) {
        Log.d("login", login.toString())
        if (login) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }, 3500) // 3초 대기
        }
        else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }, 3500) // 3초 대기
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
                // clearProfileData()

                // 로그인 화면으로 이동
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
}