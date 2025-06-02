package com.example.readingbooks

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var btnSearch: Button
    private lateinit var editSearch: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[BookViewModel::class.java]

        btnSearch = findViewById(R.id.btnSearch)
        editSearch = findViewById(R.id.editSearch)
        recycler = findViewById(R.id.recyclerBooks)
        btnLogout = findViewById(R.id.btnLogout)

        recycler.layoutManager = LinearLayoutManager(this)

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
                saveBookToSupabase(selectedBook)
            }
            recycler.adapter = adapter
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Log.d("FIREBASE", "🚪 로그아웃됨")
            finish()
        }
    }

    private fun saveBookToSupabase(selectedBook: BookDocument) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Log.e("SUPABASE", "❌ 로그인된 사용자 없음")
            return
        }

        val uid = user.uid

        val safeTitle = selectedBook.title
            .replace("&", "and")
            .replace("(", "")
            .replace(")", "")

        val book = Book(
            user_id = uid,
            title = safeTitle,
            author = selectedBook.authors.joinToString(", "),
            isbn = null,
            review = ""
        )


        val gson = com.google.gson.Gson()
        Log.d("SUPABASE", "📤 전송 JSON: ${gson.toJson(book)}")


        try {
            val client = SupabaseClient.create() // ✅ 토큰 전달 X
            Log.d("SUPABASE", "📦 SupabaseClient 생성 완료")

            client.insertBook(book).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("SUPABASE", "📥 응답 코드: ${response.code()}")
                    Log.d("SUPABASE", "📥 응답 body: ${response.body()}")
                    Log.d("SUPABASE", "📥 에러 body: ${response.errorBody()?.string()}")
                    if (response.isSuccessful) {
                        Log.d("SUPABASE", "✅ 책 저장 성공: ${book.title}")
                    } else {
                        Log.e("SUPABASE", "❌ 저장 실패: ${response.code()}, ${response.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("SUPABASE", "❌ 네트워크 오류: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("SUPABASE", "❗ 예외 발생: ${e.message}")
        }
    }

}
