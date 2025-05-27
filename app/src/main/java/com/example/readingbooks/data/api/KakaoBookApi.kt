package com.example.readingbooks.data.api

import com.example.readingbooks.data.model.BookSearchResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.Call

interface KakaoBookApi {
    @GET("v3/search/book")
    fun searchBooks(@Query("query") query: String): Call<BookSearchResponse>
}

