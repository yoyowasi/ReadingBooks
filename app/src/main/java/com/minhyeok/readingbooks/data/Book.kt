package com.minhyeok.readingbooks.data

data class Book(
    val id: Int,                      // ✅ 이 필드 추가
    val uid: String,
    val title: String,
    val author: String,
    val isbn: String,
    val thumbnailUrl: String,
    val review: String = "",
    val page_count: Int?
)



