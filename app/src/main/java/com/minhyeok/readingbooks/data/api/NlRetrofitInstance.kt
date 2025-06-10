package com.minhyeok.readingbooks.data.api

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object NlRetrofitInstance {
    val api: NlBookApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.nl.go.kr/seoji/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(NlBookApi::class.java)
    }
}