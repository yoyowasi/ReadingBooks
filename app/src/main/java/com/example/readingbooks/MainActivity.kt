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
import com.example.readingbooks.data.BookInsertRequest
import com.example.readingbooks.data.UserBookInsertRequest
import com.example.readingbooks.data.api.NlRetrofitInstance
import com.example.readingbooks.data.api.SupabaseClient
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.data.model.NlBookResponse
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

                // ✅ 다이얼로그 표시
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("📘 ${selectedBook.title}")
                    .setMessage("이 책을 내 서재에 저장하시겠습니까?")
                    .setPositiveButton("저장") { _, _ ->
                        saveBookToSupabase(selectedBook)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
            recycler.adapter = adapter
        }


    }
    // MainActivity.kt 상단에 추가 (또는 별도 Util 파일로 빼도 됨)
    private fun extractIsbn13(isbnRaw: String): String? {
        return isbnRaw.split(" ")
            .firstOrNull { it.length == 13 && (it.startsWith("978") || it.startsWith("979")) }
    }
    // Supabase에 책 정보 저장
    private fun saveBookToSupabase(selectedBook: BookDocument) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val safeTitle = selectedBook.title.replace("&", "and").replace("(", "").replace(")", "")

        // ✅ ① ISBN-13 추출
        val isbn13 = extractIsbn13(selectedBook.isbn)
        if (isbn13 == null) {
            Log.e("❌ISBN", "ISBN-13을 추출할 수 없습니다: ${selectedBook.isbn}")
            return
        }
        // ✅ ② NL API로 페이지 수 요청
        NlRetrofitInstance.api.getBookByIsbn(
            apiKey = "여기에_국립중앙도서관_API_KEY", // ← 꼭 본인 키로!
            isbn = isbn13
        ).enqueue(object : Callback<NlBookResponse> {
            override fun onResponse(call: Call<NlBookResponse>, response: Response<NlBookResponse>) {
                val nlBookItem = response.body()?.doc?.firstOrNull()
                val pageCount = nlBookItem?.pageCount?.filter { it.isDigit() }?.toIntOrNull()

                // ✅ ③ Supabase에 저장
                val bookRequest = BookInsertRequest(
                    isbn = isbn13,
                    title = safeTitle,
                    author = selectedBook.authors.joinToString(", "),
                    publisher = selectedBook.publisher,
                    thumbnail = selectedBook.thumbnail,
                    page_count = pageCount
                )

                val userBookRequest = UserBookInsertRequest(
                    user_id = uid,
                    isbn = isbn13,
                    review = "",
                    read_page = 0
                )

                val client = SupabaseClient.create()

                client.insertBook(bookRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            client.insertUserBook(userBookRequest).enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    Log.d("✅SUPABASE", "책 저장 완료")
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Log.e("❌SUPABASE", "user_books 저장 실패: ${t.message}")
                                }
                            })
                        } else {
                            Log.e("❌SUPABASE", "books 저장 실패: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("❌SUPABASE", "books 저장 네트워크 오류: ${t.message}")
                    }
                })
            }

            override fun onFailure(call: Call<NlBookResponse>, t: Throwable) {
                Log.e("❌NL API", "쪽수 가져오기 실패: ${t.message}")
            }
        })
    }

}
