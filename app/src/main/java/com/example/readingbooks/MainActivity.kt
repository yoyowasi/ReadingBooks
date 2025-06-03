package com.example.readingbooks

import android.content.Intent
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
import com.example.readingbooks.data.BookInsertRequest
import com.example.readingbooks.data.UserBookInsertRequest
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
    private lateinit var btnMyBooks: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[BookViewModel::class.java]
        btnSearch = findViewById(R.id.btnSearch)
        editSearch = findViewById(R.id.editSearch)
        recycler = findViewById(R.id.recyclerBooks)
        btnLogout = findViewById(R.id.btnLogout)
        btnMyBooks = findViewById(R.id.btnMyBooks)

        recycler.layoutManager = LinearLayoutManager(this)

        // 검색 버튼 클릭 시 ViewModel을 통해 책 검색
        btnSearch.setOnClickListener {
            val query = editSearch.text.toString()
            if (query.isNotBlank()) {
                Log.d("SEARCH", "검색어: $query")
                viewModel.searchBook(query)
            }
        }

        // 내 서재 화면으로 이동
        btnMyBooks.setOnClickListener {
            val intent = Intent(this, MyLibraryActivity::class.java)
            startActivity(intent)
        }

        // 검색 결과가 변경될 때마다 RecyclerView 업데이트
        viewModel.searchResults.observe(this) { bookDocs ->
            val adapter = SearchResultAdapter(bookDocs) { selectedBook ->
                Log.d("SEARCH_CLICK", "선택한 책: ${selectedBook.title}")
                saveBookToSupabase(selectedBook)
            }
            recycler.adapter = adapter
        }

    }

    // Supabase에 책 정보 저장
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

        val bookRequest = BookInsertRequest(
            isbn = selectedBook.isbn,
            title = safeTitle,
            author = selectedBook.authors.joinToString(", "),
            publisher = selectedBook.publisher,
            thumbnail = selectedBook.thumbnail,
            page_count = null // ← NL API 결과가 있다면 여기에 넣기
        )

        val userBookRequest = UserBookInsertRequest(
            user_id = uid,
            isbn = selectedBook.isbn,
            review = "",
            read_page = 0
        )

        val client = SupabaseClient.create()

        client.insertBook(bookRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("SUPABASE", "✅ 책 정보 저장 성공")

                    // user_books 테이블에도 저장
                    client.insertUserBook(userBookRequest).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Log.d("SUPABASE", "✅ 유저 책 저장 성공")
                            } else {
                                Log.e("SUPABASE", "❌ 유저 책 저장 실패: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("SUPABASE", "❌ 유저 책 저장 네트워크 오류: ${t.message}")
                        }
                    })
                } else {
                    Log.e("SUPABASE", "❌ 책 정보 저장 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SUPABASE", "❌ 책 정보 저장 네트워크 오류: ${t.message}")
            }
        })
    }
}
