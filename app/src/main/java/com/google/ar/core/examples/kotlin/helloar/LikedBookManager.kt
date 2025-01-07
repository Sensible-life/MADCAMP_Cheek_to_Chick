package com.google.ar.core.examples.kotlin.helloar

import com.mpackage.network.LikedBooks

object LikedBookManager {
    // 전역에서 사용할 books 리스트
    var lbooks: List<LikedBooks> = emptyList()

    // books를 업데이트하는 메소드
    fun setBooks(newBooks: List<LikedBooks>) {
        lbooks = newBooks
    }
}
