package com.example.readingbooks.data

data class UserBook(
    val user_id: String,
    val isbn: String,
    val review: String?,
    val read_page: Int,
    val created_at: String
)
