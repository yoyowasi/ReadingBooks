package com.example.readingbooks.data.api

import com.example.readingbooks.data.model.NlBookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NlBookApi {
    @GET("SearchApi.do")
    fun getBookByIsbn(
        @Query("cert_key") apiKey: String,
        @Query("result_style") style: String = "xml",
        @Query("isbn") isbn: String
    ): Call<NlBookResponse>
}