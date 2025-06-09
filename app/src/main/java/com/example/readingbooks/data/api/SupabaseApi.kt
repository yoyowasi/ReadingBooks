package com.example.readingbooks.data.api

import com.example.readingbooks.data.Book
import com.example.readingbooks.data.BookInsertRequest
import com.example.readingbooks.data.UserBook
import com.example.readingbooks.data.UserBookInsertRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query


interface SupabaseApi {

    @Headers("Prefer: return=representation")
    @POST("books")
    fun insertBook(@Body book: BookInsertRequest): Call<Void>

    @GET("books")
    fun getBookByIsbn(@Query("isbn") isbn: String): Call<List<Book>>

    @POST("user_books")
    @Headers("Prefer: return=representation")
    fun insertUserBook(@Body userBook: UserBookInsertRequest): Call<Void>

    @GET("user_books?select=*,book:books(title,author,thumbnail,page_count)")
    fun getUserBooksByUserId(@Query("user_id") userIdFilter: String): Call<List<UserBook>>

    @DELETE("user_books")
    fun deleteUserBookById(@Query("id") id: String): Call<Void>

//    @PATCH("user_books")
//    fun updateUserBookReadPageById(
//        @Query("id") id: String, // ← 이게 핵심
//        @Body readPage: Map<String, Int>
//    ): Call<Void>

    @PATCH("user_books")
    @Headers("Prefer: return=representation")
    fun updateUserBookReadPageById(
        @Query("id") idFilter: String, // eq.{id} 형식 필요!
        @Body readPage: Map<String, Int>
    ): Call<Void>

}