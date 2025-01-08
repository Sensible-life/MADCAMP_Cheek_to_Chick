package com.google.ar.core.examples.kotlin.helloar.profile

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.ar.core.examples.kotlin.helloar.R
import com.mpackage.ElevenLabsApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.webkit.MimeTypeMap
import androidx.fragment.app.DialogFragment

class VoiceSettingFragment : DialogFragment() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var selectFileButton: Button
    private lateinit var sendButton: Button
    private lateinit var voiceNameInput: EditText
    private var selectedFile: File? = null

    private val tag = "profile_upload" // 디버깅 로그 태그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView: View creation started")
        val view = inflater.inflate(R.layout.screen_profile_voice_upload, container, false)

        initViews(view)
        setupFilePicker()
        setupSendButton()

        Log.d(tag, "onCreateView: View creation completed")
        return view
    }

    // View 초기화
    private fun initViews(view: View) {
        selectFileButton = view.findViewById(R.id.selectFileButton)
        sendButton = view.findViewById(R.id.sendButton)
        voiceNameInput = view.findViewById(R.id.voiceNameInput)
    }

    private var selectedMimeType: String? = null

    private fun setupFilePicker() {
        val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val filePath = FileUtils.getPath(requireContext(), uri)
                if (filePath != null) {
                    selectedFile = File(filePath)
                    selectedMimeType = getMimeType(uri) ?: "application/octet-stream" // Default if MIME type is unknown
                    Toast.makeText(requireContext(), "File selected: ${selectedFile?.name}", Toast.LENGTH_SHORT).show()
                    Log.d(tag, "setupFilePicker: File path resolved: $filePath, MIME type: $selectedMimeType")
                } else {
                    Toast.makeText(requireContext(), "Failed to get file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        selectFileButton.setOnClickListener {
            filePicker.launch("audio/*") // Allow only audio files
        }
    }

    // 전송 버튼 설정
    private fun setupSendButton() {
        Log.d(tag, "setupSendButton: Setting up send button")
        sendButton.setOnClickListener {
            val voiceName = voiceNameInput.text.toString()
            if (voiceName.isNotEmpty() && selectedFile != null) {
                Log.d(tag, "setupSendButton: Sending file with voice name: $voiceName")
                sendToElevenLabs(voiceName, selectedFile!!)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Log.e(tag, "setupSendButton: Missing fields - voiceName: $voiceName, selectedFile: $selectedFile")
            }
        }
    }

    private fun sendToElevenLabs(voiceName: String, file: File) {
        val mimeType = selectedMimeType ?: "application/octet-stream" // MIME 타입이 없으면 기본값 사용

        // 파일 요청 바디 생성
        val fileRequestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("files", file.name, fileRequestBody)

        // API 호출
        ElevenLabsApi.uploadVoice(
            voiceName,  // name은 String으로 전달
            filePart,   // 파일은 MultipartBody.Part로 전달
            object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Voice uploaded successfully!", Toast.LENGTH_SHORT).show()
                        Log.d(tag, "sendToElevenLabs: Upload successful - Response: ${response.body()?.string()}")

                    } else {
                        Toast.makeText(requireContext(), "Upload failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e(tag, "sendToElevenLabs: Upload failed - Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e(tag, "sendToElevenLabs: Request failed - Error: ${t.message}")
                }
            }
        )
    }



    // MIME 타입 변환
    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            requireContext().contentResolver.getType(uri)
        } else {
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.let {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
            }
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
    }
}
