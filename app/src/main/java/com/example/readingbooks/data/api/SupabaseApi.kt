package com.example.readingbooks.data.api

import com.example.readingbooks.data.Book
import retrofit2.http.GET
import retrofit2.Call
import com.example.readingbooks.data.model.BookSearchResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SupabaseApi {

    @GET("books?select=*")
    fun getBooks(): Call<List<Book>>

    @POST("books")
    fun insertBook(@Body book: Book): Call<Void>
}

