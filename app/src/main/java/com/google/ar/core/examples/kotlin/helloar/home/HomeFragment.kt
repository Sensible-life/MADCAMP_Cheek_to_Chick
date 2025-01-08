package com.google.ar.core.examples.kotlin.helloar.home

import BooksApi
import ListedBookAdapter
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.LinearLayout
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ar.core.examples.kotlin.helloar.BooksDatabaseHelper
import com.google.ar.core.examples.kotlin.helloar.LikedBookManager
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.profile.VoiceData
import com.google.ar.core.examples.kotlin.helloar.profile.VoiceDto
import com.google.ar.core.examples.kotlin.helloar.profile.VoiceListAdapter
import com.google.ar.core.examples.kotlin.helloar.profile.VoiceSettingFragment
import com.google.gson.Gson
import com.mpackage.network.LikedBooks
import com.mpackage.network.ListedBooks
import com.mpackage.network.RetrofitClient
import com.mpackage.network.RetrofitClient.retrofit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var dX: Float = 0f // 뷰와 터치 지점 간의 차이를 저장
    var initialX = 0f // 터치 시작 위치 저장
    var booksFromServer: List<LikedBooks> = emptyList()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.screen_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // val voiceSetting = view.findViewById<LinearLayout>(R.id.voice_record)
        fetchAllBooksFromServer()
        var booksList: List<LikedBooks> = emptyList()
        val recyclerView: RecyclerView = view.findViewById(R.id.bookRecyclerView)

        // Initialize the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        setupSearchFunctionality()

        val icon = view.findViewById<ImageView>(R.id.logo)
        val screenHome = view.findViewById<FrameLayout>(R.id.screenHome)
        val parentView = screenHome.parent as? ViewGroup
        val bookshelf: FrameLayout = view.findViewById(R.id.bookshelf)
        val searchEditText = requireView().findViewById<EditText>(R.id.search_edit_text)
        val searchBar = view.findViewById<LinearLayout>(R.id.search_bar)
        val dragArea = view.findViewById<FrameLayout>(R.id.dragArea)
        /*searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                bookshelf.animate()
                    .x(((bookshelf.parent as View).width - 30).toFloat())
                    .setDuration(300)
                    .start()
                recyclerView.visibility = View.VISIBLE // RecyclerView 보이기

                recyclerView.animate()
                    .translationYBy(-1600f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()
                // Adjust marginBottom of searchBar
                searchBar.animate()
                    .translationYBy(-650f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()
            }
            else {
                bookshelf.animate()
                    .x(0f)
                    .setDuration(300)
                    .start()

                searchBar.animate()
                    .translationYBy(650f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()

                recyclerView.animate()
                    .translationYBy(1600f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()

                recyclerView.visibility = View.GONE // RecyclerView 숨기기
            }
        }*/

        // 데이터 불러오기
        val rootView = view

        var started = false
        var up = false
        var down = true

        var isAnimating = false // 애니메이션 중인지 여부를 판단하는 변수

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val inBounds = imm.isAcceptingText // 키보드가 올라왔는지 내려갔는지 확인

            Log.d("DEBUG", "moving by rootview")
            if (!started) {
                recyclerView.postDelayed({started = true}, 300)
            }
            // 키보드가 올라왔고, 애니메이션 중이 아니며, down이 true일 때,
            if (inBounds && down && !isAnimating) {
                isAnimating = true // 애니메이션 시작 전에 플래그 설정

                // Adjust marginBottom of searchBar
                icon.animate()
                    .translationYBy(-650f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()

                // 키보드가 올라왔을 때의 동작
                bookshelf.animate()
                    .x(((bookshelf.parent as View).width - 30).toFloat())
                    .setDuration(300)
                    .start()
                recyclerView.visibility = View.VISIBLE // RecyclerView 보이기

                recyclerView.animate()
                    .translationYBy(-1600f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()

                // Adjust marginBottom of searchBar
                searchBar.animate()
                    .translationYBy(-650f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()


                up = true
                down = false

                // 애니메이션이 끝났을 때 플래그 초기화
                recyclerView.postDelayed({
                    isAnimating = false
                }, 300) // 애니메이션 시간 동안 대기 후 플래그 초기화
            }
            // 키보드가 내려갔고, 애니메이션 중이 아니며, started가 true일 때
            else if (up && !isAnimating) {
                isAnimating = true // 애니메이션 시작 전에 플래그 설정
                icon.animate()
                    .translationYBy(650f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()
                // 키보드가 내려갔을 때의 동작
                bookshelf.animate()
                    .x(0f)
                    .setDuration(300)
                    .start()

                searchBar.animate()
                    .translationYBy(650f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()

                recyclerView.animate()
                    .translationYBy(1600f) // marginTop을 1000에서 300으로 애니메이션
                    .setDuration(300) // 애니메이션 지속 시간 (500ms)
                    .start()

                recyclerView.visibility = View.GONE // RecyclerView 숨기기

                down = true
                up = false
                // 애니메이션이 끝났을 때 플래그 초기화
                recyclerView.postDelayed({
                    isAnimating = false
                }, 300) // 애니메이션 시간 동안 대기 후 플래그 초기화
            }
        }



        // Search Bar Move


        // 검색 기능 초기화


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


        // 드래그 및 드롭 이벤트 처리
        dragArea?.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // 드래그 시작 시
                    view.setBackgroundColor(Color.LTGRAY)
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // 드래그 영역 안으로 들어왔을 때
                    view.setBackgroundColor(Color.YELLOW)
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    // 드래그 중일 때 (위치 업데이트)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // 드래그 영역을 벗어났을 때
                    view.setBackgroundColor(Color.LTGRAY)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // 드롭 시
                    val droppedView = event.localState as View
                    val x = event.x
                    val y = event.y

                    // 드롭된 뷰 위치 업데이트
                    val layoutParams = FrameLayout.LayoutParams(droppedView.width, droppedView.height)
                    layoutParams.leftMargin = x.toInt() - droppedView.width / 2
                    layoutParams.topMargin = y.toInt() - droppedView.height / 2
                    droppedView.layoutParams = layoutParams

                    // 뷰 색상 복원
                    view.setBackgroundColor(Color.LTGRAY)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // 드래그가 끝났을 때
                    view.setBackgroundColor(Color.TRANSPARENT)
                    true
                }
                else -> false
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

                    // 이동 거리(dX)가 일정 범위 이상일 때만 애니메이션 처리
                    if (Math.abs(event.rawX - initialX) > 10) {
                        v.animate()
                            .x(clampedX)
                            .setDuration(0)
                            .start()
                        Log.d("DEBUG", "Bookshelf ACTION_MOVE at $clampedX")
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val finalX = event.rawX
                    // 이동 거리가 일정 범위 이상일 때만 슬라이드 애니메이션 실행
                    if (Math.abs(finalX - initialX) > 10) {
                        if (finalX < initialX) {
                            // 왼쪽으로 슬라이드
                            Log.d("DEBUG", "Moving by bookshelf drag")
                            icon.animate()
                                .translationYBy(650f) // marginTop을 1000에서 300으로 애니메이션
                                .setDuration(300) // 애니메이션 지속 시간 (500ms)
                                .start()
                            v.animate()
                                .x(minX)
                                .setDuration(300)
                                .start()

                            searchBar.animate()
                                .translationYBy(650f)
                                .setDuration(300)
                                .start()

                            recyclerView.animate()
                                .translationYBy(1600f)
                                .setDuration(300)
                                .start()

                            recyclerView.visibility = View.GONE
                            Log.d("DEBUG", "Bookshelf ACTION_UP - Slide Left")
                        } else if (finalX > initialX) {
                            // 오른쪽으로 슬라이드
                            Log.d("DEBUG", "Moving by bookshelf drag")
                            v.animate()
                                .x(maxX)
                                .setDuration(300)
                                .start()

                            recyclerView.visibility = View.VISIBLE
                            icon.animate()
                                .translationYBy(-650f) // marginTop을 1000에서 300으로 애니메이션
                                .setDuration(300) // 애니메이션 지속 시간 (500ms)
                                .start()

                            recyclerView.animate()
                                .translationYBy(-1600f)
                                .setDuration(300)
                                .start()

                            searchBar.animate()
                                .translationYBy(-650f)
                                .setDuration(300)
                                .start()
                            Log.d("DEBUG", searchBar.layoutParams.toString())
                        }
                    }
                }
            }
            true
        }

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


    private fun fetchAllBooksFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.listedBooksApi.getAllBooks()
                if (response.isSuccessful) {
                    booksFromServer = response.body() ?: emptyList()

                    Log.d("fetchAllBooksFromServer", "Fetched ${booksFromServer.size} books from server")

                    // Generate random rankings and sort by ranking (ascending)
                    booksFromServer.forEach { it.ranking = (1..100).random() }
                    val sortedBooks = booksFromServer.sortedBy { it.ranking }

                    withContext(Dispatchers.Main) {
                        //RecyclerView 초기화
                        updateRecyclerView(booksFromServer)
                        // UI 업데이트 또는 데이터 저장 로직 추가
                        Toast.makeText(requireContext(), "Fetched ${booksFromServer.size} books", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(
                        "fetchAllBooksFromServer",
                        "Failed to fetch books: ${response.code()} - ${response.message()}"
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to fetch books", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("fetchAllBooksFromServer", "Error fetching books: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterBooksByTitle(query: String) {
        Log.d("filter", query)
        CoroutineScope(Dispatchers.IO).launch {
            val filteredBooks = booksFromServer.filter { it.title.contains(query, ignoreCase = true) } // 검색 쿼리에 따라 필터링

            withContext(Dispatchers.Main) {
                if (filteredBooks.isNotEmpty()) {
                    Log.d("filteredBooks", filteredBooks.toString())
                    updateRecyclerView(filteredBooks)
                    Toast.makeText(requireContext(), "Found ${filteredBooks.size} books", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No books found for \"$query\"", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateRecyclerView(filteredBooks: List<LikedBooks>) {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.bookRecyclerView)
        val adapter = ListedBookAdapter(filteredBooks) { book ->
            updateBookLikeStatus(book)
        }
        recyclerView.adapter = adapter
    }

    fun updateBookLikeStatus(book: LikedBooks) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                // 서버에 업데이트 요청 (title과 likes 전달)
                Log.d("title", book.title)
                val response = RetrofitClient.listedBooksApi.updateBookStatus(
                    title = book.title,
                    like = book.likes
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Book status updated", Toast.LENGTH_SHORT).show()
                        Log.d("response", response.toString())
                    } else {
                        Log.d("response", response.toString())
                        Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun setupSearchFunctionality() {
        val searchEditText = requireView().findViewById<EditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                filterBooksByTitle(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
//        searchEditText.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                val query = searchEditText.text.toString().trim()
//                filterBooksByTitle(query)
//                true
//            } else {
//                false
//            }
//        }
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