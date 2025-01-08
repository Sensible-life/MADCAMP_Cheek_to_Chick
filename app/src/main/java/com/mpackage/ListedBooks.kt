package com.mpackage.network

data class ListedBooks(
    val title: String,
    val title_img: String,
    val bookPages: List<BookPage>, // Page 대신 BookPage 사용
    var ranking: Int,
    var likes: Boolean
)

data class BookPage( // Page 대신 BookPage로 변경
    val content: String,
    val image_url: String
)
