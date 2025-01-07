package com.google.ar.core.examples.kotlin.helloar.book

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.ar.core.examples.kotlin.helloar.ContentWithPath

class BookPagerAdapter(
    private val context: Context,
    private val contentsWithPaths: List<ContentWithPath> // ContentWithPath 리스트로 수정
) : FragmentStateAdapter(context as FragmentActivity) {

    override fun getItemCount(): Int = contentsWithPaths.size

    override fun createFragment(position: Int): Fragment {
        val contentWithPath = contentsWithPaths[position]  // ContentWithPath 객체 가져오기
        return BookPageFragment.newInstance(contentWithPath.content, contentWithPath.imagePath)
    }
}
