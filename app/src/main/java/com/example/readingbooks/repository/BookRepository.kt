package com.example.readingbooks.repository

import com.example.readingbooks.data.Book
import com.example.readingbooks.data.UserBook
import com.example.readingbooks.data.api.SupabaseClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
                                id = it.book_id ?: 0, // 또는 적절한 예외 처리
                                uid = it.user_id,
                                title = it.book.title,
                                author = it.book.author ?: "",
                                isbn = it.isbn,
                                review = it.review,
                                thumbnailUrl = it.book.thumbnail,
                                page_count = it.book.page_count
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