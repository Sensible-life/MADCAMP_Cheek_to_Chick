package com.google.ar.core.examples.kotlin.helloar.book

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.BuildConfig
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.GzipSource
import okio.buffer
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class BookPageFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var content: String
    private lateinit var imagePath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_book_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageContent = arguments?.getString("content")
        val pageImagePath = arguments?.getString("imagePath")

        val textView = view.findViewById<TextView>(R.id.page_text)
        val imageView = view.findViewById<ImageView>(R.id.page_image)
        val playButton = view.findViewById<ImageView>(R.id.play_button)

        textView.text = pageContent

        if (pageImagePath != null) {
            setImageFromFile(pageImagePath, imageView) // 파일 경로에서 이미지 로드
        }

        playButton.setOnClickListener {
            if (!pageContent.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    sendTTSRequestWithFuel(pageContent)
                }
            } else {
                Log.e("BookPageFragment", "Content is null or empty")
            }
        }
    }

    companion object {
        fun newInstance(content: String, imagePath: String): BookPageFragment {
            val fragment = BookPageFragment()
            val args = Bundle()
            args.putString("content", content)
            args.putString("imagePath", imagePath)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // TTS 요청과 음성 파일 재생
    fun sendTTSRequestWithFuel(content: String) {
        val elevenLabsUrl = "https://api.elevenlabs.io/v1/text-to-speech/gtH3Ett0fWXfaYH2VOVy/stream?output_format=mp3_22050_32"
        val apiKey = BuildConfig.ELEVEN_API_KEY
        val jsonData = """
        {
            "text": "$content"
        }
    """.trimIndent()

        Fuel.post(elevenLabsUrl)
            .header(
                "xi-api-key" to "$apiKey",
                "Content-Type" to "application/json",
                "accept" to "audio/mpeg"
            )
            .body(jsonData)
            .response { _, response, result ->
                when (result) {
                    is Result.Success -> {
                        // MP3 데이터를 임시 파일로 저장
                        val tempFile = File.createTempFile("tts_audio", ".mp3")
                        tempFile.outputStream().use { outputStream ->
                            outputStream.write(result.get())
                        }
                        playAudio(tempFile)
                    }
                    is Result.Failure -> {
                        Log.e("TTS Request", "Request failed: ${result.getException().message}")
                        Log.e("TTS Request", "Response status: ${response.statusCode} - ${response.responseMessage}")
                    }
                }
            }
    }

    // 임시 파일에서 오디오 재생
    fun playAudio(file: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    it.start()
                    Log.d("Audio", "Audio playback started")
                }
                setOnCompletionListener {
                    Log.d("Audio", "Audio playback completed")
                    file.delete() // 재생 후 임시 파일 삭제
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Audio", "Error playing audio: ${e.message}")
        }
    }

    // Base64로 인코딩된 이미지를 ImageView에 표시
    fun setImageFromBase64(base64String: String, imageView: ImageView) {
        try {
            val decodedString: ByteArray = Base64.decode(base64String, Base64.NO_WRAP)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 파일 경로에서 이미지를 디코딩하여 ImageView에 표시
    fun setImageFromFile(imagePath: String, imageView: ImageView) {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
