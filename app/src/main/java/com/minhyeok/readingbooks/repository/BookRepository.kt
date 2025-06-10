package com.minhyeok.readingbooks.repository

import com.minhyeok.readingbooks.data.Book
import com.minhyeok.readingbooks.data.UserBook
import com.minhyeok.readingbooks.data.api.SupabaseClient
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
                        // ❗ UserBook → Book 변환 (null 안전성 추가)
                        val books = response.body()?.map { userBook ->
                            Book(
                                id = userBook.book_id ?: 0,
                                uid = userBook.user_id,
                                title = userBook.book.title ?: "제목 없음", // ← null 안전성
                                author = userBook.book.author ?: "저자 없음", // ← null 안전성
                                isbn = userBook.isbn,
                                review = userBook.review,
                                thumbnailUrl = userBook.book.thumbnail ?: "", // ← null 안전성
                                page_count = userBook.book.page_count
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