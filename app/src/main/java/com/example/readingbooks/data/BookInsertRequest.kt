package com.example.readingbooks.data

data class BookInsertRequest(
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String?,
    val thumbnail: String?,
    val page_count: Int? = null
)

