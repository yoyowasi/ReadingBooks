package com.example.readingbooks.data.api

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
    @POST("books?on_conflict=isbn")
    fun insertBook(@Body book: BookInsertRequest): Call<Void>

    @Headers("Prefer: return=representation")
    @POST("user_books")
    fun insertUserBook(@Body userBook: UserBookInsertRequest): Call<Void>

    @GET("user_books?select=*,book:books(title,thumbnail,page_count)")
    fun getUserBooksByUserId(@Query("user_id") userIdFilter: String): Call<List<UserBook>>

    @PATCH("user_books")
    fun updateUserBookReadPageByBookId(
        @Query("book_id") bookId: String,
        @Body readPage: Map<String, Int>
    ): Call<Void>


    @DELETE("user_books")
    fun deleteUserBookByBookId(@Query("book_id") bookId: String): Call<Void> // ✅ 수정

}


