package com.example.readingbooks.data.api

import com.example.readingbooks.data.Book
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @Headers("Prefer: return=representation")
    @POST("books")
    fun insertBook(@Body books: Book): Call<Void>  // ✅ 배열로 보냄

    // 🔽 이 부분 추가: user_id 기준으로 책 목록 조회
    @GET("books")
    fun getBooks(): Call<List<Book>>

    @GET("books")
    fun getMyBooks(@Query("user_id") userId: String): Call<List<Book>>

}

