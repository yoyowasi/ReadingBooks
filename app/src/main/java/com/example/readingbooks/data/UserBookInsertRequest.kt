package com.example.readingbooks.data

data class UserBookInsertRequest(
    val user_id: String,
    val isbn: String,
    val review: String = "",
    val read_page: Int = 0
)

