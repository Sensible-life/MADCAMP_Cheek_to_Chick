package com.mpackage
import com.google.ar.core.examples.kotlin.helloar.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Callback
import java.io.File

object ElevenLabsApi {

    private val service = ElevenLabRetrofit.retrofit.create(ElevenLabsService::class.java)

    fun uploadVoice(name: String, file: MultipartBody.Part, callback: Callback<ResponseBody>) {
        val call = service.addVoice(BuildConfig.ELEVEN_API_KEY, name, file) // name은 String으로 직접 전달
        call.enqueue(callback)
    }
}


/*object ElevenLabsApi {

    private val service = ElevenLabRetrofit.retrofit.create(ElevenLabsService::class.java)

    fun uploadVoice(name: String, file: File, callback: Callback<ResponseBody>) {
        // 이름을 RequestBody로 생성
        val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        // 파일 요청 생성
        val filePart = MultipartBody.Part.createFormData(
            "files[]",
            file.name,
            file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
        )

        val call = service.addVoice(name, filePart)
        call.enqueue(callback)
    }
}*/

