package com.google.ar.core.examples.kotlin.helloar.community

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.android.flexbox.FlexboxLayout
import com.google.ar.core.examples.kotlin.helloar.ContentWithPath
import com.google.ar.core.examples.kotlin.helloar.GPT.DalleRequest
import com.google.ar.core.examples.kotlin.helloar.GPT.GPTRepository
import com.google.ar.core.examples.kotlin.helloar.GPT.GPTRepository_book
import com.google.ar.core.examples.kotlin.helloar.GPT.GPTRepository_image
import com.google.ar.core.examples.kotlin.helloar.book.BookActivity
import com.google.ar.core.examples.kotlin.helloar.home.HomeFragment
import com.google.gson.JsonObject
import com.mpackage.network.ApiService
import com.mpackage.network.Book
import com.mpackage.network.BookSaveApi
import com.mpackage.network.Page
import com.mpackage.network.RetrofitClient
import com.mpackage.network.utils.toMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import retrofit2.Retrofit.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL


@Suppress("DEPRECATION")
class CreateFragment4 : Fragment() {
    private val repository = GPTRepository_book()
    private val repository_image = GPTRepository_image()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_create_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val previousText = arguments?.getString("previousText")
        val currentText = arguments?.getString("currentText")
        val endText = arguments?.getString("endText")
        val imageView = view.findViewById<ImageView>(R.id.cover_image) ?: return


        val selectedText = view.findViewById<TextView>(R.id.selected_text)



        // 로딩 화면 표시
        showLoadingFragment(3)

        // 프롬프트 정의
        val inputPrompt = previousText + "라는 교훈을 가진 어린이들을 위한 8페이지짜리 동화책을 만들 거야. " +
                "주인공 캐릭터는" + currentText + "이고, 대상 독자는" + endText +"의 나이를 가지고 있어." +
                "8페이지짜리 동화책을 만들어 줘. 각 페이지는 3줄 정도면 괜찮을 것 같아." +
                "이야기의 기승전결이 명확했으면 좋겠고, 내용 흐름의 연결이 자연스러우며, 중복되는 내용의 페이지가 최대한" +
                "적었으면 좋겠어. 문장 끝에는 개행문자를 넣어줘. 전체 문장 그 어디에도 쌍따옴표나, 따옴표가 들어가지" +
                "않도록 해 줘." +
                "답변의 형태는, 서론 생략하고 제목: \n 1. (내용) \n 2.(내용) \n 3. (내용) \n" +
                " 4.(내용) \n 5. (내용) \n" +
                " 6.(내용) \n 7. (내용) \n" +
                " 8.(내용) "
        var imagePrompt = ""

        // 비동기 작업으로 API 호출
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = repository.getChatResponse(inputPrompt)
                val engPrompt = response + "이거 영어로 번역해 줘"
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val eng = repository.getChatResponse(engPrompt)
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val imageUrls = generateImagesWithDelay(eng, 9, 3000L) // 12초 딜레이 적용
                                imageUrls.forEach { url ->
                                    println("Generated Image URL: $url")
                                }
                                hideLoadingFragment()
                                populateFlexboxLayout(eng, imageUrls, selectedText)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // 응답이 오면 로딩 화면을 제거하고 데이터를 표시
            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 발생 시 로딩 화면에 에러 메시지 표시
            }
        }
    }

    private fun populateFlexboxLayout(response: String, imageUrls: List<String>, selectedText: TextView) {
        // 응답을 줄 단위로 나누기
        // selectedText.text = response
        val jsonData = createJsonWithImages(response, imageUrls)
        println(jsonData.toString(4))

        // jsonData 를 서버로 전송
        Log.d("Function Call", "sendBookDataToServer called")
        sendBookDataToServer(jsonData)
        Log.d("JSON Data", jsonData.toString())

        val contents = mutableListOf<String>() // 페이지 내용 리스트
        val pagesArray = jsonData.getJSONArray("pages")

        // 각 페이지의 "content" 값을 추출
        for (i in 0 until pagesArray.length()) {
            val pageObject = pagesArray.getJSONObject(i)
            val content = pageObject.getString("content")
            contents.add(content)  // "content" 값을 contents 리스트에 추가
            Log.d("Content", contents.toString())
        }

        val imageView = view?.findViewById<ImageView>(R.id.cover_image) ?: return
        if (imageUrls.isNotEmpty()) {
            setImageFromBase64(imageUrls[0], imageView)
        }
        val savedFilePaths = mutableListOf<String>()
        val contentsWithPaths = mutableListOf<ContentWithPath>()

// content와 imagePath를 함께 묶어서 저장
        for (index in 0 until contents.size) {
            val content = contents[index]
            val imageUrl = imageUrls.getOrNull(index + 1)  // imageUrl[i+1] (0번 인덱스를 건너뜀)

            imageUrl?.let { base64Image ->
                val filename = "image_${index + 1}.jpg"
                val filePath = saveBase64ImageToFile(base64Image, requireContext(), filename)
                filePath?.let {
                    savedFilePaths.add(it)
                    // content와 imagePath를 쌍으로 묶어 contentsWithPaths에 추가
                    contentsWithPaths.add(ContentWithPath(content, it))
                }
            }
        }

// 클릭 시 imagePath와 content를 BookActivity로 전달
        imageView.setOnClickListener {
            val intent = Intent(context, BookActivity::class.java)
            val jsonParameter = jsonData.toString() // JSON 객체를 문자열로 변환
            Log.d("From fragment", jsonParameter)

            // content와 imagePath를 묶어서 전달
            intent.putExtra("contentsWithPaths", ArrayList(contentsWithPaths))  // contents와 filePaths를 함께 전달
            startActivity(intent)
        }
    }

    private fun sendBookDataToServer(jsonData: JSONObject){

        // JSON 데이터를 Book 객체로 변환
        val bookData = Book(
            title = jsonData.getString("title"),
            pages = jsonData.getJSONArray("pages").let { pagesArray ->
                List(pagesArray.length()) { i ->
                    val pageObject = pagesArray.getJSONObject(i)
                    Page(
                        content = pageObject.getString("content"),
                        image_url = pageObject.getString("image_url")
                    )
                }
            },
            title_img = jsonData.getString("title_img"),
            likes = jsonData.getBoolean("likes"),
            ranking = jsonData.getInt("ranking")
        )

        val apiService = RetrofitClient.retrofit.create(BookSaveApi::class.java)

        Log.d("Debug", "Converted JSON to Map: $bookData")

        // 코루틴을 통해 서버로 데이터 전송
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.sendBookData(bookData) // suspend fun 호출
                Log.d("Server Response", "Book data successfully sent to server")
            } catch (e: Exception) {
                Log.e("Server Error", "Error sending book data: ${e.message}")
            }
        }

    }




    private fun showLoadingFragment(pageNumber: Int) {
        val loadingFragment = LoadingFragment.newInstance(pageNumber)
        parentFragmentManager.beginTransaction()
            .add(R.id.bigger_content_frame, loadingFragment, "LoadingFragment")
            .commit()
    }

    private fun hideLoadingFragment() {
        // 로딩 화면 제거
        val loadingFragment = parentFragmentManager.findFragmentByTag("LoadingFragment")
        if (loadingFragment != null) {
            parentFragmentManager.beginTransaction()
                .remove(loadingFragment)
                .commit()
        }
    }


    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    fun createJsonWithImages(response: String, imageUrls: List<String>): JSONObject {
        val json = JSONObject()

        // 제목 추출
        val titleRegex = Regex("^Title: (.+)")
        val title = titleRegex.find(response)?.groups?.get(1)?.value ?: "제목 없음"
        json.put("title", title)

        // 내용 추출
        val contentRegex = Regex("\\d+\\.\\s*(.+)")
        val matches = contentRegex.findAll(response)

        val pagesArray = JSONArray()
        json.put("title_img", imageUrls[0])

        matches.forEachIndexed { index, matchResult ->
            val pageObject = JSONObject()
            pageObject.put("content", matchResult.groups[1]?.value ?: "내용 없음")
            pageObject.put("image_url", if (index < imageUrls.size) imageUrls[index] else null) // 이미지 URL 매핑
            pagesArray.put(pageObject)
        }

        json.put("pages", pagesArray)

        json.put("likes", true)
        json.put("ranking", 0)


        return json
    }

    /*fun parseResponseToJson(response: String): JSONObject {
        val json = JSONObject()

        // 제목 추출
        val titleRegex = Regex("^제목: (.+)")
        val title = titleRegex.find(response)?.groups?.get(1)?.value ?: "제목 없음"
        json.put("title", title)

        // 내용 추출
        val contentRegex = Regex("\\d+\\.\\s*(.+)")
        val matches = contentRegex.findAll(response)

        val contentsArray = JSONArray()
        matches.forEach { matchResult ->
            contentsArray.put(matchResult.groups[1]?.value ?: "내용 없음")
        }

        json.put("contents", contentsArray)

        return json
    }*/

    suspend fun generateImagesWithDelay(prompt: String, totalImages: Int, delayMillis: Long): List<String> {
        val imageUrls = mutableListOf<String>()
        val (title, contents) = parseResponseToList(prompt)
        val batches = totalImages / 1 // 1개의 이미지를 한 번에 생성
        repeat(batches) { batchIndex ->
            try {
                // 개별 요청 생성
                var requestPrompt = prompt
                if (batchIndex == 0) requestPrompt = title + ". Create the book cover of this book." +
                        " I want it to have the feel of a traditional Korean folktale book cover."
                else {
                    requestPrompt = "Create an image that match the following sentences in " +
                            "the style of traditional Korean folktales: " +
                            contents[batchIndex - 1] + "."
                }

                val response = repository_image.generateImage(requestPrompt)

                // 생성된 이미지 URL 추가
                response.forEach { imageUrl ->
                    val base64Image = encodeImageToBase64(imageUrl)  // 이미지 URL을 Base64로 인코딩
                    imageUrls.add(base64Image)  // Base64로 변환된 이미지를 추가
                }

                // 딜레이 추가
                if (batchIndex < batches - 1) {
                    delay(delayMillis)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error in batch ${batchIndex + 1}: ${e.message}")
            }
        }

        return imageUrls
    }

    fun parseResponseToList(response: String): Pair<String, List<String>> {
        // 제목 추출
        val titleRegex = Regex("^Title:\\s*(.+)")
        val title = titleRegex.find(response)?.groups?.get(1)?.value ?: "No Title"

        // 페이지 내용 추출
        val pageRegex = Regex("\\d+\\.\\s*(.+)")
        val matches = pageRegex.findAll(response)

        val contentsList = mutableListOf<String>()
        matches.forEach { matchResult ->
            contentsList.add(matchResult.groups[1]?.value ?: "No Content")
        }

        return Pair(title, contentsList)
    }


    suspend fun encodeImageToBase64(imageUrl: String): String {
        return try {
            // 네트워크 작업을 IO 스레드에서 처리
            withContext(Dispatchers.IO) {
                // URL에서 이미지 다운로드
                val url = URL(imageUrl)
                val inputStream: InputStream = url.openStream()

                // Bitmap으로 변환
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Bitmap을 ByteArray로 변환
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                // Base64로 인코딩
                Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "" // 실패 시 빈 문자열 반환
        }
    }

    fun setImageFromBase64(base64String: String, imageView: ImageView) {
        try {
            // Base64 문자열을 디코딩하여 바이트 배열로 변환
            val decodedString: ByteArray = Base64.decode(base64String, Base64.NO_WRAP)

            // 바이트 배열을 Bitmap으로 변환
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            // Bitmap을 ImageView에 설정
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveBase64ImageToFile(base64String: String, context: Context, filename: String): String? {
        return try {
            // Base64 문자열을 디코딩하여 바이트 배열로 변환
            val decodedString: ByteArray = Base64.decode(base64String, Base64.NO_WRAP)

            // 바이트 배열을 Bitmap으로 변환
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            // 파일 경로 정의 (앱의 내부 저장소)
            val file = File(context.filesDir, filename)

            // 파일에 Bitmap을 PNG 형식으로 저장
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // 저장된 파일의 경로 반환
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null // 오류 발생 시 null 반환
        }
    }
}