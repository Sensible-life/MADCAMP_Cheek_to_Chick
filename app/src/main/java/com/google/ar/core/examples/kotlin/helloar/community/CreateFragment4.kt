package com.google.ar.core.examples.kotlin.helloar.community

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
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
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.android.flexbox.FlexboxLayout
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
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import retrofit2.Retrofit.*








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
        showLoadingFragment()

        // 프롬프트 정의
        val inputPrompt = previousText + "라는 교훈을 가진 어린이들을 위한 2페이지짜리 동화책을 만들 거야. " +
                "주인공 캐릭터는" + currentText + "이고, 대상 독자는" + endText +"의 나이를 가지고 있어." +
                "2페이지짜리 동화책을 만들어 줘. 각 페이지는 3줄 정도면 괜찮을 것 같아." +
                "이야기의 기승전결이 명확했으면 좋겠고, 내용 흐름의 연결이 자연스러우며, 중복되는 내용의 페이지가 최대한" +
                "적었으면 좋겠어. 문장 끝에는 개행문자를 넣어줘. 전체 문장 그 어디에도 쌍따옴표나, 따옴표가 들어가지" +
                "않도록 해 줘." +
                "답변의 형태는, 서론 생략하고 제목: \n 1. (내용) \n 2.(내용) \n"
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
                                val imageUrls = generateImagesWithDelay(eng, 3, 3000L) // 12초 딜레이 적용
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


        val imageView = view?.findViewById<ImageView>(R.id.cover_image) ?: return
        if (imageUrls.isNotEmpty()) {
            Glide.with(requireContext())
                .load(imageUrls[0]) // 첫 번째 URL 사용 (표지 이미지)
                .into(imageView)
        }
        imageView.setOnClickListener {
            val intent = Intent(context, BookActivity::class.java)
            val jsonParameter = jsonData.toString() // JSON 객체를 문자열로 변환
            Log.d("From fragment", jsonParameter)
            intent.putExtra("bookData", jsonParameter)
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
            }
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




    private fun showLoadingFragment() {
        // 로딩 화면 표시
        parentFragmentManager.beginTransaction()
            .add(R.id.bigger_content_frame, LoadingFragment(), "LoadingFragment")
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
        matches.forEachIndexed { index, matchResult ->
            val pageObject = JSONObject()
            pageObject.put("content", matchResult.groups[1]?.value ?: "내용 없음")
            pageObject.put("image_url", if (index < imageUrls.size) imageUrls[index] else null) // 이미지 URL 매핑
            pagesArray.put(pageObject)
        }

        json.put("pages", pagesArray)
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
                imageUrls.addAll(response)

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


}