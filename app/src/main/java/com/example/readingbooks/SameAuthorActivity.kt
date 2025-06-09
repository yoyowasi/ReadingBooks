package com.example.readingbooks

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readingbooks.adapter.SearchResultAdapter
import com.example.readingbooks.data.UserBookInsertRequest
import com.example.readingbooks.data.api.SupabaseClient
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.databinding.ActivitySameAuthorBooksBinding
import com.example.readingbooks.viewmodel.BookViewModel
import com.example.readingbooks.data.api.RetrofitInstance
import com.example.readingbooks.data.model.BookSearchResponse
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SameAuthorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySameAuthorBooksBinding
    private lateinit var adapter: SearchResultAdapter
    private lateinit var bookViewModel: BookViewModel
    private val searchResults = mutableListOf<BookDocument>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AUTHOR_ACTIVITY", "SameAuthorActivity onCreate 호출됨")
        binding = ActivitySameAuthorBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "관련 도서"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookViewModel = ViewModelProvider(this)[BookViewModel::class.java]

        val searchQuery = intent.getStringExtra("AUTHOR_NAME") ?: ""
        Log.d("AUTHOR_INTENT", "넘겨받은 searchQuery=$searchQuery")

        binding.textSameAuthorTitle.text = if (searchQuery.isNotBlank()) {
            "\"$searchQuery\" 관련 책"
        } else {
            "검색 정보 없음"
        }

        // 📝 책을 터치했을 때 저장 다이얼로그 표시
        adapter = SearchResultAdapter(searchResults) { bookDoc ->
            showSaveBookDialog(bookDoc)
        }
        binding.recyclerSameAuthor.layoutManager = LinearLayoutManager(this)
        binding.recyclerSameAuthor.adapter = adapter

        if (searchQuery.isBlank()) {
            Toast.makeText(this, "검색 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchBooksByQuery(searchQuery)
    }

// 📝 간단한 책 저장 다이얼로그 (리뷰, 페이지 입력 제거)
private fun showSaveBookDialog(bookDoc: BookDocument) {
    AlertDialog.Builder(this)
        .setTitle("📚 책 저장")
        .setMessage("\"${bookDoc.title}\"\n저자: ${bookDoc.authors.joinToString(", ")}\n\n이 책을 내 서재에 추가하시겠습니까?")
        .setPositiveButton("저장") { _, _ ->
            saveBookToLibrary(bookDoc)
        }
        .setNegativeButton("취소", null)
        .show()
}

// 📝 책을 내 서재에 저장 (기본값 사용)
private fun saveBookToLibrary(bookDoc: BookDocument) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
        return
    }

    // ISBN 추출 (첫 번째 ISBN 사용)
    val isbn = bookDoc.isbn.split(" ").firstOrNull() ?: ""
    if (isbn.isBlank()) {
        Toast.makeText(this, "ISBN 정보가 없어 저장할 수 없습니다.", Toast.LENGTH_SHORT).show()
        return
    }

    val userBookRequest = UserBookInsertRequest(
        user_id = currentUser.uid,
        isbn = isbn,
        review = "", // 📝 기본값: 빈 문자열
        read_page = 0 // 📝 기본값: 0페이지
    )

    Log.d("SAVE_BOOK", "저장 요청: $userBookRequest")

    SupabaseClient.create().insertUserBook(userBookRequest)
        .enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("✅SAVE_BOOK", "책 저장 성공")
                    Toast.makeText(this@SameAuthorActivity, "✅ \"${bookDoc.title}\"이(가) 내 서재에 추가되었습니다!", Toast.LENGTH_LONG).show()
                    
                    // 📝 저장 후 액티비티 종료하고 MyLibraryActivity로 돌아가기
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("❌SAVE_BOOK", "책 저장 실패: ${response.code()} - $errorBody")
                    
                    // 중복 저장 에러 처리
                    if (response.code() == 409 || errorBody?.contains("duplicate") == true) {
                        Toast.makeText(this@SameAuthorActivity, "❌ 이미 내 서재에 있는 책입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SameAuthorActivity, "❌ 저장에 실패했습니다: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("❌SAVE_BOOK", "네트워크 오류: ${t.message}")
                Toast.makeText(this@SameAuthorActivity, "❌ 네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
}

    private fun fetchBooksByQuery(query: String) {
        Log.d("SEARCH", "API 검색 query=$query")
        RetrofitInstance.api.searchBooks(query)
            .enqueue(object : Callback<BookSearchResponse> {
                override fun onResponse(
                    call: Call<BookSearchResponse>,
                    response: Response<BookSearchResponse>
                ) {
                    if (response.isSuccessful) {
                        val docs = response.body()?.documents ?: emptyList()
                        Log.d("SEARCH_RESULT", "검색 결과 ${docs.size}권")
                        searchResults.clear()
                        searchResults.addAll(docs)
                        adapter.notifyDataSetChanged()
                        if (docs.isEmpty()) {
                            Toast.makeText(this@SameAuthorActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("SEARCH_RESULT", "API 실패 code=${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                    Log.e("SEARCH_RESULT", "API 네트워크 오류: ${t.message}")
                }
            })
    }

    // 📝 뒤로 가기 버튼 처리
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}