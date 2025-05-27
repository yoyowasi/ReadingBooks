package com.example.readingbooks.repository

import android.util.Log
import com.example.readingbooks.data.Book
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
            val client = SupabaseClient.create(token)

            client.getBooks().enqueue(object : Callback<List<Book>> {
                override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                    if (response.isSuccessful) {
                        onResult(response.body() ?: emptyList())
                    } else {
                        onError(Exception("불러오기 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                    onError(t)
                }
            })
        }
    }

}
