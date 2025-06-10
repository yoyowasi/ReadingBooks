package com.minhyeok.readingbooks.viewmodel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.minhyeok.readingbooks.data.Book
import com.minhyeok.readingbooks.data.model.BookDocument
import com.minhyeok.readingbooks.data.api.RetrofitInstance
import com.minhyeok.readingbooks.data.model.BookSearchResponse
import com.minhyeok.readingbooks.repository.BookRepository

class BookViewModel : ViewModel() {

    val searchResults = MutableLiveData<List<BookDocument>>() // ✅ 이것만 남기세요

    val myBooks = MutableLiveData<List<Book>>()

    fun fetchMyBooks() {
        BookRepository.getBooksFromSupabase(
            onResult = { myBooks.value = it },
            onError = { Log.e("BookViewModel", "내 서재 불러오기 오류: ${it.message}") }
        )
    }


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






