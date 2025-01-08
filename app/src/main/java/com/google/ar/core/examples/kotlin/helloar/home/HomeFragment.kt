package com.google.ar.core.examples.kotlin.helloar.home


import ListedBookAdapter
import android.annotation.SuppressLint
import android.content.ClipData
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.BooksDatabaseHelper
import com.google.ar.core.examples.kotlin.helloar.R
import com.mpackage.network.LikedBooks
import com.mpackage.network.ListedBooks
import com.mpackage.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var dX: Float = 0f // 뷰와 터치 지점 간의 차이를 저장
    var initialX = 0f // 터치 시작 위치 저장


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        // 데이터 불러오기
        fetchAllBooksFromServer()
        val view = inflater.inflate(R.layout.screen_home, container, false)

        // Find RecyclerView in the layout
        val recyclerView: RecyclerView = view.findViewById(R.id.bookRecyclerView)

        // Initialize the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        // Search Bar Move
        val searchBar = view.findViewById<LinearLayout>(R.id.search_bar)

        // 검색 기능 초기화
        setupSearchFunctionality()



        val screenHome = view.findViewById<FrameLayout>(R.id.screenHome)
        val parentView = screenHome.parent as? ViewGroup
        val dragArea = parentView?.findViewById<FrameLayout>(R.id.dragArea)
        fetchBooksFromServer()
        fetchAllBooksFromServer()


        val bookshelf: FrameLayout = view.findViewById(R.id.bookshelf)
        val book1: ImageView = view.findViewById(R.id.book_1)
        val book2: ImageView = view.findViewById(R.id.book_2)
        val book3: ImageView = view.findViewById(R.id.book_3)
        val book4: ImageView = view.findViewById(R.id.book_4)
        val book5: ImageView = view.findViewById(R.id.book_5)
        val book6: ImageView = view.findViewById(R.id.book_6)

// book1~book6에 동일한 LongClickListener 설정
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
                        recyclerView.visibility = View.INVISIBLE // RecyclerView 숨기기
                        Log.d("DEBUG", "Bookshelf ACTION_UP - Slide Left")
                    } else if (finalX > initialX) {
                        // 오른쪽으로 슬라이드
                        v.animate()
                            .x(maxX)
                            .setDuration(300)
                            .start()
                        recyclerView.visibility = View.VISIBLE // RecyclerView 보이기
                        // Adjust marginBottom of searchBar
                        val layoutParams = searchBar.layoutParams as ViewGroup.MarginLayoutParams
                        layoutParams.bottomMargin = 300
                        searchBar.layoutParams = layoutParams
                        Log.d("DEBUG", "Bookshelf ACTION_UP - Slide Right")
                    }
                }
            }
            true
        }


        return view
    }
// ERASE IT //
//    private fun saveBooksToDatabase(books: List<LikedBooks>) {
//        val dbHelper = BooksDatabaseHelper(requireContext())
//        dbHelper.insertBooks(books)
//    }
//
//    private fun loadBooksFromDatabase(): List<LikedBooks> {
//        val dbHelper = BooksDatabaseHelper(requireContext())
//        return dbHelper.getAllBooks()
//    }

    private fun fetchBooksFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.booksApi.getLikedBooks()
                Log.d("response", response.toString())
                if (response.isSuccessful) {
                    // 서버 응답 데이터(body)를 LikedBooks 리스트로 변환
                    val likedBooks: List<LikedBooks> = response.body() ?: emptyList()

                    Log.d("김문원", "saved books to database")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Books saved successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to fetch books", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun fetchAllBooksFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.listedBooksApi.getAllBooks()
                if (response.isSuccessful) {
                    val books: List<ListedBooks> = response.body() ?: emptyList()
                    Log.d("fetchAllBooksFromServer", "Fetched ${books.size} books from server")

                    // Generate random rankings and sort by ranking (ascending)
                    books.forEach { it.ranking = (1..100).random() }
                    val sortedBooks = books.sortedBy { it.ranking }

                    withContext(Dispatchers.Main) {
                        //RecyclerView 초기화
                        updateRecyclerView(booksFromServer)
                        // UI 업데이트 또는 데이터 저장 로직 추가
                        Toast.makeText(requireContext(), "Fetched ${books.size} books", Toast.LENGTH_SHORT).show()
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

    private var booksFromServer: List<ListedBooks> = emptyList()

    private fun filterBooksByTitle(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredBooks = booksFromServer.filter { it.title.contains(query, ignoreCase = true) } // 검색 쿼리에 따라 필터링

            withContext(Dispatchers.Main) {
                if (filteredBooks.isNotEmpty()) {
                    updateRecyclerView(filteredBooks)
                    Toast.makeText(requireContext(), "Found ${filteredBooks.size} books", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No books found for \"$query\"", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateRecyclerView(filteredBooks: List<ListedBooks>) {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.itemlistbook)
        val adapter = ListedBookAdapter(filteredBooks) { book ->
            updateBookLikeStatus(book)
        }
        recyclerView.adapter = adapter
    }

    fun updateBookLikeStatus(book: ListedBooks) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                // 서버에 업데이트 요청 (title과 likes 전달)
                val response = RetrofitClient.listedBooksApi.updateBookStatus(
                    title = book.title,
                    isLiked = book.likes
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Book status updated", Toast.LENGTH_SHORT).show()
                    } else {
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

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString().trim()
                filterBooksByTitle(query)
                true
            } else {
                false
            }
        }
    }





}