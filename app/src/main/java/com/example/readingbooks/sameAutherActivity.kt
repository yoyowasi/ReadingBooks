package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readingbooks.adapter.SearchResultAdapter
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.data.model.NlBookResponse
import com.example.readingbooks.databinding.ActivitySameAuthorBooksBinding
import com.example.readingbooks.viewmodel.BookViewModel
import com.example.readingbooks.data.api.NlRetrofitInstance
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
        binding = ActivitySameAuthorBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "동일 저자 도서"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookViewModel = ViewModelProvider(this)[BookViewModel::class.java]

        val authorName = intent.getStringExtra("AUTHOR_NAME") ?: ""
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
    // Kakao 책 API는 'query'에 저자명, 'target'에 'person'을 주면 저자 검색이 됩니다.
    RetrofitInstance.api.searchBooks(author)
        .enqueue(object : Callback<BookSearchResponse> {
            override fun onResponse(
                call: Call<BookSearchResponse>,
                response: Response<BookSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val docs = response.body()?.documents ?: emptyList()
                    searchResults.clear()
                    searchResults.addAll(docs)
                    adapter.notifyDataSetChanged()
                    if (docs.isEmpty()) {
                        Toast.makeText(this@SameAuthorActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SameAuthorActivity, "도서 검색 실패", Toast.LENGTH_SHORT).show()
                    Log.e("SameAuthorActivity", "API error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                Toast.makeText(this@SameAuthorActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("SameAuthorActivity", "API failure: ${t.message}")
            }
        })
}
}