package com.example.readingbooks

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readingbooks.adapter.BookAdapter
import com.example.readingbooks.adapter.SearchResultAdapter
import com.example.readingbooks.data.Book
import com.example.readingbooks.data.api.SupabaseClient
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[BookViewModel::class.java]

        val recycler = findViewById<RecyclerView>(R.id.recyclerBooks)
        recycler.layoutManager = LinearLayoutManager(this)

        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val editSearch = findViewById<EditText>(R.id.editSearch)

        btnSearch.setOnClickListener {
            val query = editSearch.text.toString()
            if (query.isNotBlank()) {
                Log.d("SEARCH", "검색어: $query")
                viewModel.searchBook(query)
            }
        }
        viewModel.searchResults.observe(this) { bookDocs ->
            val adapter = SearchResultAdapter(bookDocs) { selectedBook ->
                Log.d("SEARCH_CLICK", "선택한 책: ${selectedBook.title}")
            }
            recycler.adapter = adapter
        }
    }

    private fun saveBookToSupabase(selectedBook: BookDocument) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        user.getIdToken(true).addOnSuccessListener { result ->
            val token = result.token ?: return@addOnSuccessListener
            val uid = user.uid

            // BookDocument → Book 변환
            val book = Book(
                uid = uid,
                title = selectedBook.title,
                author = selectedBook.authors.joinToString(", "),
                isbn = selectedBook.isbn,
                review = ""
            )

            val client = SupabaseClient.create(token)
            client.insertBook(book).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("SUPABASE", "✅ 책 저장 성공: ${book.title}")
                    } else {
                        Log.e("SUPABASE", "❌ 저장 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("SUPABASE", "❌ 네트워크 오류: ${t.message}")
                }
            })
        }
    }


}



