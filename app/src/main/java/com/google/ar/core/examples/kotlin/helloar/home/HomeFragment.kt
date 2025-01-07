package com.google.ar.core.examples.kotlin.helloar.home

import BooksApi
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.ar.core.examples.kotlin.helloar.BooksDatabaseHelper
import com.google.ar.core.examples.kotlin.helloar.LikedBookManager
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.gson.Gson
import com.mpackage.network.LikedBooks
import com.mpackage.network.RetrofitClient
import com.mpackage.network.RetrofitClient.retrofit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class HomeFragment : Fragment() {

    private var dX: Float = 0f // 뷰와 터치 지점 간의 차이를 저장
    var initialX = 0f // 터치 시작 위치 저장


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var booksList: List<LikedBooks> = emptyList()
        val view = inflater.inflate(R.layout.screen_home, container, false)
        val screenHome = view.findViewById<FrameLayout>(R.id.screenHome)
        val parentView = screenHome.parent as? ViewGroup
        val bookshelf: FrameLayout = view.findViewById(R.id.bookshelf)
        val book1: ImageView = view.findViewById(R.id.book_1)
        val book2: ImageView = view.findViewById(R.id.book_2)
        val book3: ImageView = view.findViewById(R.id.book_3)
        val book4: ImageView = view.findViewById(R.id.book_4)
        val book5: ImageView = view.findViewById(R.id.book_5)
        val book6: ImageView = view.findViewById(R.id.book_6)

        book1.setTag(R.id.book_1, 0)
        book2.setTag(R.id.book_2, 1)
        book3.setTag(R.id.book_3, 2)
        book4.setTag(R.id.book_4, 3)
        book5.setTag(R.id.book_5, 4)
        book6.setTag(R.id.book_6, 5)

        CoroutineScope(Dispatchers.Main).launch {
            val books = fetchBooksFromServer()
            LikedBookManager.setBooks(books)
            Log.d("book", books.toString())
            // books 리스트를 UI에 반영하는 코드
            if (books.isNotEmpty()) {
                for (i in 0 until minOf(books.size, 6)) {
                    val gson = Gson()
                    val jsonData = gson.toJson(books[i])  // 각 책에 대해 처리
                    val jsonD = JSONObject(jsonData)
                    Log.d("iterator", i.toString())
                    // "title_img"를 가져와서 Base64 디코딩
                    val firstImageUrl = jsonD.getString("title_img")
                    if (firstImageUrl.isNotEmpty()) {
                        val decodedImage = decodeBase64Image(firstImageUrl)

                        // 디코딩된 이미지를 해당 ImageView에 설정
                        decodedImage?.let {
                            val imageView = when (i) {
                                0 -> book1
                                1 -> book2
                                2 -> book3
                                3 -> book4
                                4 -> book5
                                5 -> book6
                                else -> null
                            }

                            imageView?.setImageBitmap(it)
                        }
                    }
                }
            } else {
                Log.d("Books", "No books found")
            }
        }

        listOf(book1, book2, book3, book4, book5, book6).forEach { book ->
            book.setOnLongClickListener { view ->
                val clipData = ClipData.newPlainText("", "") // 드래그 데이터를 설정 (비어 있음)
                val shadow = View.DragShadowBuilder(view) // 드래그 시 그림자 효과
                view.startDragAndDrop(clipData, shadow, view, 0)
                true
            }
        }

        // 초기 위치: 화면 오른쪽 바깥
        bookshelf.translationX = 1000f

        // 애니메이션으로 화면 안으로 이동
        bookshelf.animate()
            .translationX(0f)
            .setDuration(500)
            .start()


        // bookshelf TouchListener 설정
        bookshelf.setOnTouchListener { v, event ->
            val parentWidth = (v.parent as View).width // 부모 레이아웃의 너비
            val viewWidth = v.width // 뷰의 너비
            val minX = 0f // 이동 가능한 최소 X 위치
            val maxX = (parentWidth - 30).toFloat() // 이동 가능한 최대 X 위치

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = event.rawX - v.x
                    initialX = event.rawX
                    Log.d("DEBUG", "Bookshelf ACTION_DOWN at ${event.rawX}, dX: $dX")
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX - dX
                    val clampedX = newX.coerceIn(minX, maxX)
                    v.animate()
                        .x(clampedX)
                        .setDuration(0)
                        .start()
                    Log.d("DEBUG", "Bookshelf ACTION_MOVE at $clampedX")
                }
                MotionEvent.ACTION_UP -> {
                    val finalX = event.rawX
                    if (finalX < initialX) {
                        // 왼쪽으로 슬라이드
                        v.animate()
                            .x(minX)
                            .setDuration(300)
                            .start()
                        Log.d("DEBUG", "Bookshelf ACTION_UP - Slide Left")
                    } else if (finalX > initialX) {
                        // 오른쪽으로 슬라이드
                        v.animate()
                            .x(maxX)
                            .setDuration(300)
                            .start()
                        Log.d("DEBUG", "Bookshelf ACTION_UP - Slide Right")
                    }
                }
            }
            true
        }
        return view
    }


    private suspend fun fetchBooksFromServer(): List<LikedBooks> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.booksApi.getLikedBooks()
                Log.d("response", response.toString())
                if (response.isSuccessful) {
                    // 서버 응답 데이터(body)를 LikedBooks 리스트로 변환하여 반환
                    val likedBooks: List<LikedBooks> = response.body() ?: emptyList()
                    Log.d("김문원", "saved books to database")
                    likedBooks // 성공 시 LikedBooks 리스트 반환
                } else {
                    // 실패 시 빈 리스트 반환
                    Log.d("김문원", "Failed to fetch books")
                    emptyList<LikedBooks>()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 예외 처리 시 메인 스레드에서 토스트 메시지 표시
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                emptyList<LikedBooks>() // 예외 발생 시 빈 리스트 반환
            }
        }
    }

    fun decodeBase64Image(base64String: String): Bitmap? {
        return try {
            // Base64 문자열을 바이트 배열로 디코딩
            val decodedBytes: ByteArray = Base64.decode(base64String, Base64.NO_WRAP)

            // 바이트 배열을 Bitmap으로 변환
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null // 실패하면 null 반환
        }
    }
}