package com.google.ar.core.examples.kotlin.helloar.profile

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.ar.core.examples.kotlin.helloar.R
import com.mpackage.ElevenLabsApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.webkit.MimeTypeMap

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var selectFileButton: Button
    private lateinit var sendButton: Button
    private lateinit var voiceNameInput: EditText
    private var selectedFile: File? = null

    private val tag = "profile_upload" // 디버깅 로그 태그

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView: View creation started")
        val view = inflater.inflate(R.layout.screen_profile, container, false)
        Log.d(tag, "onCreateView: View creation completed")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 초기화
        initViews(view)
        // SharedPreferences에서 사용자 정보 로드
        loadUserProfile()
        val voiceSetting = view.findViewById<LinearLayout>(R.id.voice_record)
        val languageSetting = view.findViewById<LinearLayout>(R.id.language)
        val backArrow = view.findViewById<ImageView>(R.id.backButton)
        backArrow.visibility = View.VISIBLE

        backArrow.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        voiceSetting.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,  // 뒤로 가기 버튼을 눌렀을 때, 새 fragment가 왼쪽에서 오른쪽으로 들어옴
                    R.anim.slide_out_right  // 기존 fragment가 오른쪽으로 나감
                )
                .replace(R.id.content_frame, VoiceListFragment())
                .addToBackStack(null)
                .commit()
        }
        languageSetting.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,  // 뒤로 가기 버튼을 눌렀을 때, 새 fragment가 왼쪽에서 오른쪽으로 들어옴
                    R.anim.slide_out_right  // 기존 fragment가 오른쪽으로 나감
                )
                .replace(R.id.content_frame, LanguageFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // View 초기화
    private fun initViews(view: View) {
        Log.d(tag, "initViews: Initializing views")
        profileImage = view.findViewById(R.id.profileImage)
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)
    }

    // SharedPreferences에서 사용자 프로필 정보 로드
    private fun loadUserProfile() {
        Log.d(tag, "loadUserProfile: Loading user profile")
        val sharedPreferences = requireContext().getSharedPreferences("ProfileSharedPreferences", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Unknown User")
        val email = sharedPreferences.getString("email", "No Email")
        val profileImageUrl = sharedPreferences.getString("profileImage", null)

        userName.text = username
        userEmail.text = email

        if (!profileImageUrl.isNullOrEmpty()) {
            Log.d(tag, "loadUserProfile: Loading profile image from URL: $profileImageUrl")
            Glide.with(this)
                .load(profileImageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(tag, "loadUserProfile: Image load failed: ${e?.localizedMessage}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(tag, "loadUserProfile: Image loaded successfully")
                        return false
                    }
                })
                .into(profileImage)
        } else {
            Log.e(tag, "loadUserProfile: Profile image URL is null or empty")
            profileImage.setImageResource(R.drawable.default_profile_image)
        }
    }
}
