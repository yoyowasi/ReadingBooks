package com.example.readingbooks.data.model

import com.google.gson.annotations.SerializedName

data class BookSearchResponse(
    val documents: List<BookDocument>
)

data class BookDocument(
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: List<String>,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("isbn13") val isbn13: String,
    @SerializedName("isbn10") val isbn10: String
)

