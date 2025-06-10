package com.minhyeok.readingbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minhyeok.readingbooks.adapter.SearchResultAdapter
import com.minhyeok.readingbooks.data.BookInsertRequest
import com.minhyeok.readingbooks.data.UserBookInsertRequest
import com.minhyeok.readingbooks.data.api.NlRetrofitInstance
import com.minhyeok.readingbooks.data.api.SupabaseClient
import com.minhyeok.readingbooks.data.model.BookDocument
import com.minhyeok.readingbooks.data.model.NlBookResponse
import com.minhyeok.readingbooks.viewmodel.BookViewModel
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

        btnSearch.setOnClickListener {
            val query = editSearch.text.toString()
            if (query.isNotBlank()) {
                Log.d("SEARCH", "검색어: $query")
                viewModel.searchBook(query)
            }
        }

        btnMyBooks.setOnClickListener {
            val intent = Intent(this, MyLibraryActivity::class.java)
            startActivity(intent)
        }

        viewModel.searchResults.observe(this) { bookDocs ->
            val adapter = SearchResultAdapter(bookDocs) { selectedBook ->
                Log.d("SEARCH_CLICK", "선택한 책: ${selectedBook.title}")

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

    private fun extractIsbn13(isbnRaw: String): String? {
        return isbnRaw.split(" ")
            .firstOrNull { it.length == 13 && (it.startsWith("978") || it.startsWith("979")) }
    }

    private fun saveBookToSupabase(selectedBook: BookDocument) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val isbn13 = extractIsbn13(selectedBook.isbn)
        if (isbn13 == null) {
            Log.e("❌ISBN", "ISBN-13을 추출할 수 없습니다: ${selectedBook.isbn}")
            Toast.makeText(this, "❌ ISBN-13이 없어서 저장할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val safeTitle = selectedBook.title.replace("&", "and").replace("(", "").replace(")", "")
        val author = selectedBook.authors.joinToString(", ").ifBlank { "저자 미상" }

        NlRetrofitInstance.api.getBookByIsbn(
            apiKey = "6bc9b1452d94118c24e99e8cf5af1ea00bfc2c87790e6bbc85d73f34eca709f6",
            isbn = isbn13
        ).enqueue(object : Callback<NlBookResponse> {
            override fun onResponse(call: Call<NlBookResponse>, response: Response<NlBookResponse>) {
                val nlBookItem = response.body()?.doc?.firstOrNull()
                val pageCount = nlBookItem?.pageCount?.filter { it.isDigit() }?.toIntOrNull()

                val bookRequest = BookInsertRequest(
                    isbn = isbn13,
                    title = safeTitle,
                    author = author,
                    publisher = selectedBook.publisher,
                    thumbnail = selectedBook.thumbnail,
                    page_count = pageCount
                )

                saveBookOrFetchExisting(bookRequest) { success, errorMessage ->
                    if (success) {
                        val userBookRequest = UserBookInsertRequest(
                            user_id = uid,
                            isbn = isbn13, // ❗ 무조건 ISBN-13로 저장
                            review = "",
                            read_page = 0
                        )

                        SupabaseClient.create().insertUserBook(userBookRequest)
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        Log.d("✅SUPABASE", "user_books 저장 완료")
                                    } else {
                                        Log.e("❌SUPABASE", "user_books 저장 실패: ${response.code()} ${response.errorBody()?.string()}")
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Log.e("❌SUPABASE", "user_books 저장 실패: ${t.message}")
                                }
                            })

                    } else {
                        Log.e("❌SUPABASE", errorMessage ?: "책 저장 실패")
                    }
                }
            }

            override fun onFailure(call: Call<NlBookResponse>, t: Throwable) {
                Log.e("❌NL API", "쪽수 가져오기 실패: ${t.message}")
            }
        })
    }


    private fun saveBookOrFetchExisting(book: BookInsertRequest, onResult: (Boolean, String?) -> Unit) {
        SupabaseClient.create().insertBook(book).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 201 || response.code() == 200) {
                    onResult(true, null)
                } else if (response.code() == 409) {
                    onResult(true, null)  // 이미 존재해도 다음 로직으로 진행해야 하므로 true 처리
                } else {
                    onResult(false, "저장 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, "저장 실패: ${t.message}")
            }
        })
    }
}
