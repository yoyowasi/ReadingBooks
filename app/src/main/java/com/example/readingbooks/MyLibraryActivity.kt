package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readingbooks.adapter.BookAdapter
import com.example.readingbooks.data.Book
import com.example.readingbooks.data.api.SupabaseClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.readingbooks.data.api.SupabaseApi


class MyLibraryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var adapter: BookAdapter
    private val bookList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        recycler = findViewById(R.id.recyclerMyBooks)
        btnLogout = findViewById(R.id.btnLogout)

        adapter = BookAdapter(bookList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // 로그아웃 버튼 동작
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 저장한 책 불러오기
        fetchBooks()
    }

    private fun fetchBooks() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        user.getIdToken(true).addOnSuccessListener { result ->
            val token = result.token ?: return@addOnSuccessListener
            val client = SupabaseClient.create()

            client.getMyBooks(user.uid).enqueue(object : Callback<List<Book>> {
                override fun onResponse(
                    call: Call<List<Book>>,
                    response: Response<List<Book>>
                ) {
                    if (response.isSuccessful) {
                        bookList.clear()
                        bookList.addAll(response.body() ?: emptyList())
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("SUPABASE", "❌ 책 불러오기 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                    Log.e("SUPABASE", "❌ 네트워크 오류: ${t.message}")
                }
            })
        }
    }
}
