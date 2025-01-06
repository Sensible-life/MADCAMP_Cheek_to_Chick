package com.google.ar.core.examples.kotlin.helloar.book

import android.media.MediaPlayer
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_book_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageContent = arguments?.getString("content")
        val pageImageUrl = arguments?.getString("imageUrl")

        val textView = view.findViewById<TextView>(R.id.page_text)
        val imageView = view.findViewById<ImageView>(R.id.page_image)
        val playButton = view.findViewById<Button>(R.id.play_button)

        textView.text = pageContent

        context?.let {
            Glide.with(it).load(pageImageUrl).into(imageView)
        }

        playButton.setOnClickListener {
            if (!pageContent.isNullOrEmpty()) {
                // requestAndPlayTTS(pageContent)
                CoroutineScope(Dispatchers.Main).launch {
                    sendTTSRequestWithFuel(pageContent)
                }
            } else {
                Log.e("BookPageFragment", "Content is null or empty")
            }
        }
    }

    companion object {
        fun newInstance(content: String, imageUrl: String): BookPageFragment {
            val fragment = BookPageFragment()
            val args = Bundle()
            args.putString("content", content)
            args.putString("imageUrl", imageUrl)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun sendTTSRequestWithFuel(content: String) {
        val elevenLabsUrl = "https://api.elevenlabs.io/v1/text-to-speech/e79ZA2i13VgSZjumnows/stream?output_format=mp3_22050_32"
        // val apiKey = BuildConfig.ELEVEN_API_KEY
        val apiKey = BuildConfig.ELEVEN_API_KEY
        // JSON 요청 데이터 생성
        val jsonData = """
        {
            "text": "$content"
        }
    """.trimIndent()

        // 요청 실행
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
                        println("Response received successfully")

                        // MP3 데이터를 임시 파일로 저장
                        val tempFile = File.createTempFile("tts_audio", ".mp3")
                        tempFile.outputStream().use { outputStream ->
                            outputStream.write(result.get())
                        }
                        println("Audio file saved at: ${tempFile.absolutePath}")

                        // MP3 파일 재생
                        playAudio(tempFile)
                    }
                    is Result.Failure -> {
                        println("Request failed: ${result.getException().message}")
                        println("Response status: ${response.statusCode} - ${response.responseMessage}")
                    }
                }
            }
    }

    fun playAudio(file: File) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    it.start()
                    println("Audio playback started")
                }
                setOnCompletionListener {
                    println("Audio playback completed")
                    file.delete() // 재생 후 임시 파일 삭제
                }
                setOnErrorListener { _, what, extra ->
                    println("Error during audio playback: what=$what, extra=$extra")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error playing audio: ${e.message}")
        }
    }

}
