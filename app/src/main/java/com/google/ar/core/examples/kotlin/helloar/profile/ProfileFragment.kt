package com.google.ar.core.examples.kotlin.helloar.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.R
import android.widget.ImageView
import android.widget.TextView
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target // 올바른 Target import 추가

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView

    private val TAG = "ProfileFragment" // TAG 변수 정의

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.screen_profile, container, false)

        // View 초기화
        profileImage = view.findViewById(R.id.profileImage)
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)

        // SharedPreferences에서 사용자 정보 로드
        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        // SharedPreferences에서 데이터를 가져옴
        val sharedPreferences = requireContext().getSharedPreferences("ProfileSharedPreferences", Context.MODE_PRIVATE)

        val username = sharedPreferences.getString("username", "Unknown User") // 기본값: Unknown User
        val email = sharedPreferences.getString("email", "No Email") // 기본값: No Email
        val profileImageUrl = sharedPreferences.getString("profileImage", null) // 기본값: null

        // TextView에 데이터 설정
        userName.text = username
        userEmail.text = email

        // Glide로 프로필 이미지 로드 (Glide 디버깅 추가)
        if (!profileImageUrl.isNullOrEmpty()) {
            Log.d(TAG, "Loading profile image from URL: $profileImageUrl")

            Glide.with(this)
                .load(profileImageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Glide 로딩 실패 로그
                        Log.e(TAG, "Image load failed: ${e?.localizedMessage}")
                        return false // 기본 이미지를 표시하려면 false를 반환
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Glide 로딩 성공 로그
                        Log.d(TAG, "Image loaded successfully")
                        return false
                    }
                })
                .into(profileImage)
        } else {
            // URL이 null 또는 비어 있을 때 로그 출력
            Log.e(TAG, "Profile image URL is null or empty")
            profileImage.setImageResource(R.drawable.default_profile_image) // 기본 이미지 설정
        }
    }
}
