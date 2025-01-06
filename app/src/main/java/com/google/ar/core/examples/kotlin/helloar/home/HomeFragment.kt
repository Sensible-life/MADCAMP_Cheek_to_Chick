package com.google.ar.core.examples.kotlin.helloar.home

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
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.R

class HomeFragment : Fragment() {

    private var dX: Float = 0f // 뷰와 터치 지점 간의 차이를 저장
    var initialX = 0f // 터치 시작 위치 저장


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.screen_home, container, false)
        val screenHome = view.findViewById<FrameLayout>(R.id.screenHome)
        val parentView = screenHome.parent as? ViewGroup
        val dragArea = parentView?.findViewById<FrameLayout>(R.id.dragArea)

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
}