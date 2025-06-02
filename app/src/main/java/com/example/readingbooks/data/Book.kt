package com.example.readingbooks.data

data class Book(
    val user_id: String,
    val title: String,
    val author: String,
    val isbn: String?,  // ← ✅ nullable 처리
    val review: String = ""
)


