package com.google.ar.core.examples.kotlin.helloar.book

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.ar.core.examples.kotlin.helloar.R
import org.json.JSONObject

class BookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)    //자동 생성 상단바 없앰
        setContentView(R.layout.activity_book)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.offscreenPageLimit = 3 // 좌우 3페이지씩 미리 로드


        // 전달받은 JSON 데이터
        val jsonData = intent.getStringExtra("bookData")
        if (jsonData.isNullOrEmpty()) {
            finish() // 데이터가 없으면 Activity 종료
            return
        }
        Log.d("Received", jsonData)

        try {
            // JSON 파싱
            val jsonObject = JSONObject(jsonData)
            val title = jsonObject.getString("title")
            val pagesArray = jsonObject.getJSONArray("pages") // pages 배열

            val contents = mutableListOf<String>()
            val imageUrls = mutableListOf<String>()

            for (i in 0 until pagesArray.length()) {
                val pageObject = pagesArray.getJSONObject(i)
                contents.add(pageObject.getString("content"))
                imageUrls.add(pageObject.getString("image_url"))
            }

            // ViewPager2에 어댑터 설정
            val adapter = BookPagerAdapter(this, contents, imageUrls)
            viewPager.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
            finish() // JSON 파싱 실패 시 Activity 종료
        }

    }
}
