package com.example.readingbooks.data

data class Book(
    val uid: String, // ← 추가
    val title: String,
    val author: String,
    val isbn: String,
    val thumbnailUrl: String,
    val review: String = "",
    val page_count: Int?
)



