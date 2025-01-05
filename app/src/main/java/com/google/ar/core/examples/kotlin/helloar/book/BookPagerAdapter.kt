package com.google.ar.core.examples.kotlin.helloar.book

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BookPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val contents: List<String>,
    private val imageUrls: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = contents.size

    override fun createFragment(position: Int): Fragment {
        return BookPageFragment.newInstance(contents[position], imageUrls[position])
    }
}
