package com.minhyeok.readingbooks.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://dapi.kakao.com/"
    private const val API_KEY = "KakaoAK d463be1994c4bb29e337f22d80443279"

    val api: KakaoBookApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", API_KEY)
                    .build()
                chain.proceed(request)
            }.build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoBookApi::class.java)
    }
}

