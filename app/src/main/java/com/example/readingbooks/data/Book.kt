package com.example.readingbooks.data

data class Book(
    val uid: String,
    val title: String,
    val author: String,
    val isbn: String,
    val review: String = ""
)
