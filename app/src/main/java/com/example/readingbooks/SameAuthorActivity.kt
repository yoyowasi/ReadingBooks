package com.example.readingbooks

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readingbooks.adapter.SearchResultAdapter
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.databinding.ActivitySameAuthorBooksBinding
import com.example.readingbooks.viewmodel.BookViewModel
import com.example.readingbooks.data.api.RetrofitInstance
import com.example.readingbooks.data.model.BookSearchResponse
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

        supportActionBar?.title = "동일 저자 도서"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookViewModel = ViewModelProvider(this)[BookViewModel::class.java]

        val authorName = intent.getStringExtra("AUTHOR_NAME") ?: ""
        Log.d("AUTHOR_INTENT", "넘겨받은 authorName=$authorName") // 2️⃣

        binding.textSameAuthorTitle.text = if (authorName.isNotBlank()) {
            "\"$authorName\" 저자의 책"
        } else {
            "저자 정보 없음"
        }

        // SearchResultAdapter와 동일하게 아이템 클릭 처리
        adapter = SearchResultAdapter(searchResults) { bookDoc ->
            Toast.makeText(this, "${bookDoc.title} 선택됨", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerSameAuthor.layoutManager = LinearLayoutManager(this)
        binding.recyclerSameAuthor.adapter = adapter

        if (authorName.isBlank()) {
            Toast.makeText(this, "저자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchBooksByAuthor(authorName)
    }

    private fun fetchBooksByAuthor(author: String) {
        Log.d("AUTHOR_SEARCH", "API 검색 author=$author") // 3️⃣
        RetrofitInstance.api.searchBooks(author)
            .enqueue(object : Callback<BookSearchResponse> {
                override fun onResponse(
                    call: Call<BookSearchResponse>,
                    response: Response<BookSearchResponse>
                ) {
                    if (response.isSuccessful) {
                        val docs = response.body()?.documents ?: emptyList()
                        Log.d("AUTHOR_RESULT", "검색 결과 ${docs.size}권") // 4️⃣
                        searchResults.clear()
                        searchResults.addAll(docs)
                        adapter.notifyDataSetChanged()
                        if (docs.isEmpty()) {
                            Toast.makeText(this@SameAuthorActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("AUTHOR_RESULT", "API 실패 code=${response.code()}") // 5️⃣
                    }
                }

                override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                    Log.e("AUTHOR_RESULT", "API 네트워크 오류: ${t.message}") // 6️⃣
                }
            })
    }

}