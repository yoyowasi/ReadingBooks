package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var btnSameAuthor: Button

    private lateinit var adapter: UserBookAdapter
    private val userBookList = mutableListOf<UserBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        recycler = findViewById(R.id.recyclerMyBooks)
        btnLogout = findViewById(R.id.btnLogout)
        btnSameAuthor = findViewById(R.id.btnSameAuthor)

        adapter = UserBookAdapter(userBookList) { userBook ->
            showBookActionDialog(userBook)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 오류 수정: 리스트가 비어있거나 book이 null일 때를 대비한 안전한 접근 필요
        btnSameAuthor.setOnClickListener {
            val firstBook = userBookList.firstOrNull()
            val authorName = firstBook?.book?.author ?: run {
                Toast.makeText(this, "책 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, SameAuthorActivity::class.java)
            intent.putExtra("AUTHOR_NAME", authorName)
            startActivity(intent)
        }

        fetchBooks()
    }

    private fun showBookActionDialog(userBook: UserBook) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "읽은 페이지 수 입력"

        AlertDialog.Builder(this)
            .setTitle("📖 ${userBook.book?.title ?: "책 제목 없음"}")
            .setMessage("읽은 페이지를 입력하거나 삭제할 수 있습니다.")
            .setView(input)
            .setPositiveButton("저장") { _, _ ->
                val newPage = input.text.toString().toIntOrNull() ?: 0
                updateReadPage(userBook.id, newPage) // ✅ 여기!
            }
            .setNeutralButton("삭제") { _, _ ->
                deleteBook(userBook.id)
            }
            .setNegativeButton("취소", null)
            .show()

    }


    private fun deleteBook(userBookId: String) {
        SupabaseClient.create().deleteUserBookById("eq.$userBookId")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("✅SUPABASE", "삭제 완료")
                        fetchBooks() // 리스트 새로고침
                    } else {
                        Log.e("❌SUPABASE", "삭제 실패: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("❌SUPABASE", "삭제 실패: ${t.message}")
                }
            })
    }


    private fun updateReadPage(id: String   , page: Int) {
        SupabaseClient.create().updateUserBookReadPageById(id, mapOf("read_page" to page))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("✅SUPABASE", "읽은 페이지 수정 완료")
                        fetchBooks()
                    } else {
                        Log.e("❌SUPABASE", "읽은 페이지 수정 실패: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("❌SUPABASE", "읽은 페이지 수정 실패: ${t.message}")
                }
            })
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