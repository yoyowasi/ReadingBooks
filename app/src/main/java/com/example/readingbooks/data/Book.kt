package com.example.readingbooks.data

data class Book(
    val uid: String, // ← 추가
    val title: String,
    val author: String,
    val isbn: String,
    val review: String = ""
)



