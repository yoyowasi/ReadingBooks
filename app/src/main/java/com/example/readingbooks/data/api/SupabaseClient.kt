package com.example.readingbooks.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    fun create(idToken: String): SupabaseApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0dHpraGdzcGJxeW1ucWVpbG16Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4MjUxMDAsImV4cCI6MjA2MzQwMTEwMH0.ja0kpDNzDp7_s5fZ2idTc-_EspIxUtJpk4-TG2TgApE")
                    .addHeader("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0dHpraGdzcGJxeW1ucWVpbG16Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4MjUxMDAsImV4cCI6MjA2MzQwMTEwMH0.ja0kpDNzDp7_s5fZ2idTc-_EspIxUtJpk4-TG2TgApE")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }.build()

        return Retrofit.Builder()
            .baseUrl("https://ittzkhgspbqymnqeilmz.supabase.co/rest/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(SupabaseApi::class.java)
    }
}
