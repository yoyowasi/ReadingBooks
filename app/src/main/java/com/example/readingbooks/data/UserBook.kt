package com.example.readingbooks.data

data class UserBook(
    val book_id: Int, // ✅ 숫자 기반 기본 키
    val user_id: String,
    val isbn: String,
    val review: String,
    val read_page: Int,
    val book: BookInfo
)






