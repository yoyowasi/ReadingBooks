package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readingbooks.adapter.UserBookAdapter
import com.example.readingbooks.data.UserBook
import com.example.readingbooks.data.api.SupabaseClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyLibraryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var adapter: UserBookAdapter
    private val userBookList = mutableListOf<UserBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        recycler = findViewById(R.id.recyclerMyBooks)
        btnLogout = findViewById(R.id.btnLogout)

        adapter = UserBookAdapter(userBookList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        fetchBooks()
    }

    private fun fetchBooks() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        SupabaseClient.create().getUserBooksByUserId("eq.$uid")
            .enqueue(object : Callback<List<UserBook>> {
                override fun onResponse(call: Call<List<UserBook>>, response: Response<List<UserBook>>) {
                    if (response.isSuccessful) {
                        val books = response.body() ?: emptyList()
                        Log.d("✅SUPABASE", "${books.size}권 불러옴")

                        userBookList.clear()
                        userBookList.addAll(books)
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("❌SUPABASE", "불러오기 실패: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<UserBook>>, t: Throwable) {
                    Log.e("❌SUPABASE", "네트워크 오류: ${t.message}")
                }
            })
    }
}
