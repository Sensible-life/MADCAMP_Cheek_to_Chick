package com.google.ar.core.examples.kotlin.helloar.book

import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.ar.core.examples.kotlin.helloar.ContentWithPath
import com.google.ar.core.examples.kotlin.helloar.R
import org.json.JSONObject

@Suppress("DEPRECATION")
class BookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)    // 자동 생성 상단바 없앰
        setContentView(R.layout.activity_book)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.offscreenPageLimit = 3 // 좌우 3페이지씩 미리 로드

        // 전달받은 JSON 데이터
        // val jsonData = intent.getStringExtra("bookData")
        val contentsWithPaths = intent.getSerializableExtra("contentsWithPaths") as? ArrayList<ContentWithPath>


        if (contentsWithPaths.isNullOrEmpty()) {
            finish() // 데이터가 없으면 Activity 종료
            return
        }

        try {
            // JSON 파싱 (title과 pages 정보는 그대로 유지)

            // contentsWithPaths가 전달된 경우, 이를 ViewPager에 전달하기 위해 어댑터에 설정
            val adapter = BookPagerAdapter(this, contentsWithPaths)
            viewPager.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
            finish() // JSON 파싱 실패 시 Activity 종료
        }
    }
}
