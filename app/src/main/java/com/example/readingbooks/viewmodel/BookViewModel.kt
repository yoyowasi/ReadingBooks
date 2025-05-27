package com.example.readingbooks.viewmodel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.readingbooks.data.Book
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.data.model.NlBookResponse
import com.example.readingbooks.data.api.NlRetrofitInstance
import com.example.readingbooks.data.api.RetrofitInstance
import com.example.readingbooks.data.model.BookSearchResponse
import com.example.readingbooks.repository.BookRepository

class BookViewModel : ViewModel() {

    val searchResults = MutableLiveData<List<BookDocument>>() // ✅ 이것만 남기세요

    fun searchBook(query: String) {
        RetrofitInstance.api.searchBooks(query).enqueue(object : Callback<BookSearchResponse> {
            override fun onResponse(
                call: Call<BookSearchResponse>,
                response: Response<BookSearchResponse>
            ) {
                if (response.isSuccessful) {
                    searchResults.value = response.body()?.documents ?: emptyList()
                    Log.d("BookViewModel", "검색 결과: ${searchResults.value?.size}개")
                } else {
                    Log.e("BookViewModel", "응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                Log.e("BookViewModel", "요청 실패: ${t.message}")
            }
        })
    }
}






