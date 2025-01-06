package com.google.ar.core.examples.kotlin.helloar.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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

        val bookshelf: ImageView = view.findViewById(R.id.bookshelf)
        val book1: ImageView = view.findViewById(R.id.book_1)

        // 초기 위치: 화면 오른쪽 바깥
        bookshelf.translationX = 1000f

        // 애니메이션으로 화면 안으로 이동
        bookshelf.animate()
            .translationX(0f)
            .setDuration(500)
            .start()

        // 드래그 동작 추가
        bookshelf.setOnTouchListener { v, event ->
            val parentWidth = (v.parent as View).width // 부모 레이아웃의 너비
            val viewWidth = v.width // 뷰의 너비
            val minX = 0f // 이동 가능한 최소 X 위치
            val maxX = (parentWidth - 30).toFloat() // 이동 가능한 최대 X 위치


            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 터치 시작 위치 저장
                    dX = event.rawX - v.x
                    initialX = event.rawX // 터치 시작 지점 기록
                    Log.d("DEBUG", "ACTION_DOWN at ${event.rawX}, dX: $dX")
                }
                MotionEvent.ACTION_MOVE -> {
                    // 뷰 이동 중, 새로운 X 좌표 계산
                    val newX = event.rawX - dX

                    // 범위 내에서만 이동
                    val clampedX = newX.coerceIn(minX, maxX)
                    v.animate()
                        .x(clampedX)
                        .setDuration(0)
                        .start()

                    Log.d("DEBUG", "ACTION_MOVE at $clampedX")
                }
                MotionEvent.ACTION_UP -> {
                    // 드래그 종료 시, 방향에 따라 이동
                    val finalX = event.rawX
                    if (finalX < initialX) {
                        // 왼쪽 슬라이드: 뷰를 minX로 이동
                        v.animate()
                            .x(minX) // minX로 이동
                            .setDuration(300) // 첫 애니메이션
                            .withEndAction {
                                v.animate()
                                    .x(minX + 100) // 첫 번째 튕김 (minX + 100)
                                    .setDuration(250)
                                    .withEndAction {
                                        v.animate()
                                            .x(minX) // 다시 minX로 이동
                                            .setDuration(200)
                                            .withEndAction {
                                                v.animate()
                                                    .x(minX + 50) // 두 번째 튕김 (minX + 50)
                                                    .setDuration(150)
                                                    .withEndAction {
                                                        v.animate()
                                                            .x(minX) // 최종 위치로 복귀
                                                            .setDuration(150)
                                                            .start()
                                                    }
                                                    .start()
                                            }
                                            .start()
                                    }
                                    .start()
                            }
                            .start()
                        Log.d("DEBUG", "ACTION_UP - Slide Left to minX")
                    } else if (finalX > initialX) {
                        // 오른쪽 슬라이드: 뷰를 maxX로 이동
                        v.animate()
                            .x(maxX)
                            .setDuration(300) // 부드러운 이동
                            .start()
                        Log.d("DEBUG", finalX.toString())
                        Log.d("DEBUG", initialX.toString())
                        Log.d("DEBUG", "ACTION_UP - Slide Right to maxX")
                    }
                }
            }
            true
        }

        return view
    }
}