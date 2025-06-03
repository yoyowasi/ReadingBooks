package com.example.readingbooks.repository

import android.util.Log
import com.example.readingbooks.data.Book
import com.example.readingbooks.data.UserBook
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.readingbooks.data.api.SupabaseClient


object BookRepository {

    fun getBooksFromSupabase(
        onResult: (List<Book>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        user.getIdToken(true).addOnSuccessListener { result ->
            val token = result.token ?: return@addOnSuccessListener
            val client = SupabaseClient.create()

            client.getUserBooksByUserId(user.uid).enqueue(object : Callback<List<UserBook>> {
                override fun onResponse(call: Call<List<UserBook>>, response: Response<List<UserBook>>) {
                    if (response.isSuccessful) {
                        // ❗ 만약 어댑터가 Book 기준이면 UserBook → Book 변환 필요
                        val books = response.body()?.map {
                            Book(
                                uid = it.user_id,
                                title = "", // books 테이블과 조인하지 않으면 제목 없음
                                author = "",
                                isbn = it.isbn,
                                review = it.review ?: ""
                            )
                        } ?: emptyList()

                        onResult(books)
                    } else {
                        onError(Exception("불러오기 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<UserBook>>, t: Throwable) {
                    onError(t)
                }
            })

        }
    }

}
