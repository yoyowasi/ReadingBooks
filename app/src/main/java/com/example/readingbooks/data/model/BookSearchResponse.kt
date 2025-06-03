package com.example.readingbooks.data.model

import com.google.gson.annotations.SerializedName

data class BookSearchResponse(
    val documents: List<BookDocument>
)

data class BookDocument(
    val title: String,
    val authors: List<String>,
    val publisher: String,
    val thumbnail: String,
    val contents: String,
    val isbn: String
)


