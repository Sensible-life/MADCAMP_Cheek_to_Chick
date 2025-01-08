package com.google.ar.core.examples.kotlin.helloar.profile

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R
import java.io.File

class VoiceListFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.screen_profile_voice_list, container, false)

        // SharedPreferences에서 사용자 정보 로드
        // loadUserProfile()

        Log.d(tag, "onCreateView: View creation completed")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // val voiceSetting = view.findViewById<LinearLayout>(R.id.voice_record)

        val uploadButton = view.findViewById<CardView>(R.id.upload_button)
        uploadButton.setOnClickListener {
            val dialog = VoiceSettingFragment()
            dialog.show(parentFragmentManager, "MyDialogFragment")
        }
        val voiceList: MutableList<VoiceDto> = VoiceData.getVoiceDataList()
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = VoiceListAdapter(context = requireContext(), dataList = voiceList)
    }

    // SharedPreferences에서 사용자 프로필 정보 로드
    /*private fun loadUserProfile() {
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




    // FileUtils 유틸리티
    object FileUtils {
        private val tag: String = "fileUri" // 로그 태그 정의
        fun getPath(context: Context, uri: Uri): String? {
            Log.d(tag, "FileUtils: Resolving file path for URI: $uri")
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        val displayName = it.getString(displayNameIndex)
                        val file = File(context.cacheDir, displayName)
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            file.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        Log.d(tag, "FileUtils: File resolved to: ${file.absolutePath}")
                        return file.absolutePath
                    }
                }
            }
            Log.e(tag, "FileUtils: Failed to resolve file path")
            return null
        }
    }*/
}
