package com.google.ar.core.examples.kotlin.helloar.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.cardview.widget.CardView
import com.google.ar.core.examples.kotlin.helloar.MainActivity
import com.google.ar.core.examples.kotlin.helloar.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {



    private val TAG = "KaKaoLogin"



    override fun onCreate(savedInstanceState: Bundle?) {

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
}