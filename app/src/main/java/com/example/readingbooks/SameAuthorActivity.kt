package com.example.readingbooks

import android.os.Bundle
import android.text.InputType
import android.util.Log

import android.view.MenuItem

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.readingbooks.adapter.SearchResultAdapter

import com.example.readingbooks.data.Book
import com.example.readingbooks.data.BookInsertRequest
import com.example.readingbooks.data.UserBookInsertRequest
import com.example.readingbooks.data.api.NlRetrofitInstance
import com.example.readingbooks.data.api.RetrofitInstance

import com.example.readingbooks.data.api.SupabaseClient
import com.example.readingbooks.data.model.BookDocument
import com.example.readingbooks.data.model.BookSearchResponse
import com.example.readingbooks.data.model.NlBookResponse
import com.example.readingbooks.databinding.ActivitySameAuthorBooksBinding
import com.example.readingbooks.viewmodel.BookViewModel


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
        binding = ActivitySameAuthorBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)



        supportActionBar?.title = "동일 저자 도서"


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookViewModel = ViewModelProvider(this)[BookViewModel::class.java]


        val authorName = intent.getStringExtra("AUTHOR_NAME") ?: ""


        binding.textSameAuthorTitle.text = if (searchQuery.isNotBlank()) {
            "\"$searchQuery\" 관련 책"
        } else {
            "검색 정보 없음"
        }


        adapter = SearchResultAdapter(searchResults) { selectedBook ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📘 ${selectedBook.title}")
                .setMessage("이 책을 내 서재에 저장하시겠습니까?")
                .setPositiveButton("저장") { _, _ ->
                    saveBookToSupabase(selectedBook)
                }
                .setNegativeButton("취소", null)
                .show()

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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // 현재 액티비티 종료 = 뒤로가기 효과
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun fetchBooksByAuthor(author: String) {
        fetchUserBookIsbns { ownedIsbns ->
            RetrofitInstance.api.searchBooks(author)
                .enqueue(object : Callback<BookSearchResponse> {
                    override fun onResponse(
                        call: Call<BookSearchResponse>,
                        response: Response<BookSearchResponse>
                    ) {
                        if (response.isSuccessful) {
                            val docs = response.body()?.documents ?: emptyList()

                            val filteredDocs = docs.filter { doc ->
                                val isbn13 = extractIsbn13(doc.isbn)
                                isbn13 != null && isbn13 !in ownedIsbns
                            }

                            searchResults.clear()
                            searchResults.addAll(filteredDocs)
                            adapter.notifyDataSetChanged()

                            if (filteredDocs.isEmpty()) {
                                Toast.makeText(this@SameAuthorActivity, "이미 해당 저자의 모든 책을 보유하고 계십니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("AUTHOR_RESULT", "API 실패 code=${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                        Log.e("AUTHOR_RESULT", "API 네트워크 오류: ${t.message}")
                    }
                })
        }
    }

    private fun fetchUserBookIsbns(onResult: (List<String>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        SupabaseClient.create().getUserBooksByUserId("eq.$uid")
            .enqueue(object : Callback<List<com.example.readingbooks.data.UserBook>> {

                override fun onResponse(
                    call: Call<List<com.example.readingbooks.data.UserBook>>,
                    response: Response<List<com.example.readingbooks.data.UserBook>>
                ) {
                    if (response.isSuccessful) {

                        val isbns = response.body()?.map { it.isbn }?.distinct() ?: emptyList()
                        onResult(isbns)
                    } else {
                        Log.e("❌SUPABASE", "user_books 불러오기 실패: ${response.code()}")
                        onResult(emptyList())
                    }
                }

                override fun onFailure(call: Call<List<com.example.readingbooks.data.UserBook>>, t: Throwable) {
                    Log.e("❌SUPABASE", "user_books 네트워크 오류: ${t.message}")
                    onResult(emptyList())
                }
            })
    }

    private fun extractIsbn13(isbnRaw: String): String? {
        return isbnRaw.split(" ")
            .firstOrNull { it.length == 13 && (it.startsWith("978") || it.startsWith("979")) }
    }

    private fun saveBookToSupabase(selectedBook: BookDocument) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val safeTitle = selectedBook.title.replace("&", "and").replace("(", "").replace(")", "")
        val isbn13 = extractIsbn13(selectedBook.isbn)

        if (isbn13 == null) {
            Log.e("❌ISBN", "ISBN-13을 추출할 수 없습니다: ${selectedBook.isbn}")
            return
        }

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
                    author = selectedBook.authors.joinToString(", "),
                    publisher = selectedBook.publisher,
                    thumbnail = selectedBook.thumbnail,
                    page_count = pageCount
                )

                saveBookOrFetchExisting(bookRequest) { success, errorMessage ->
                    if (success) {
                        SupabaseClient.create().getBookByIsbn("eq.$isbn13")
                            .enqueue(object : Callback<List<Book>> {
                                override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                                    val bookList = response.body()
                                    if (!bookList.isNullOrEmpty()) {
                                        val userBookRequest = UserBookInsertRequest(
                                            user_id = uid,
                                            isbn = isbn13,
                                            review = "",
                                            read_page = 0
                                        )
                                        SupabaseClient.create().insertUserBook(userBookRequest)
                                            .enqueue(object : Callback<Void> {
                                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                    if (response.isSuccessful) {
                                                        Log.d("✅SUPABASE", "user_books 저장 완료")
                                                        Toast.makeText(this@SameAuthorActivity, "서재에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Log.e("❌SUPABASE", "user_books 저장 실패: ${response.code()}")
                                                    }
                                                }

                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                    Log.e("❌SUPABASE", "user_books 저장 실패: ${t.message}")
                                                }
                                            })
                                    } else {
                                        Log.e("❌SUPABASE", "isbn으로 책을 찾을 수 없음")
                                    }
                                }

                                override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                                    Log.e("❌SUPABASE", "book_id 조회 실패: ${t.message}")
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
                if (response.code() == 201 || response.code() == 200 || response.code() == 409) {
                    onResult(true, null)
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
