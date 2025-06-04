package com.example.readingbooks.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private const val SUPABASE_URL = "https://ittzkhgspbqymnqeilmz.supabase.co/rest/v1/"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0dHpraGdzcGJxeW1ucWVpbG16Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4MjUxMDAsImV4cCI6MjA2MzQwMTEwMH0.ja0kpDNzDp7_s5fZ2idTc-_EspIxUtJpk4-TG2TgApE" // 실제 키 그대로 사용하세요


    fun create(): SupabaseApi {
        val gson = GsonBuilder()
            .disableHtmlEscaping()
            .create()
        // 🔹 헤더 설정용 OkHttpClient
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()

                Log.d("SUPABASE", "📡 요청 헤더: ${request.headers}")
                chain.proceed(request)
            }.build()

        return Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(SupabaseApi::class.java)
    }
    fun updateUserBookReadPageById(id: String, page: Int): Call<Void> {
        return create().updateUserBookReadPageById("eq.$id", mapOf("read_page" to page))
    }




}