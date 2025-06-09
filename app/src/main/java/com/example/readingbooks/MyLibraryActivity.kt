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

        // ⬇️ 롱클릭, 클릭 콜백 둘 다 넘겨줍니다
        adapter = UserBookAdapter(
            userBookList,
            onBookLongClick = { authorName ->
                Log.d("LONGCLICK", "롱클릭 authorName=$authorName")
                try {
                    val intent = Intent(this, SameAuthorActivity::class.java)
                    intent.putExtra("AUTHOR_NAME", authorName)
                    Log.d("LONGCLICK", "✅ startActivity 호출 전")
                    startActivity(intent)
                    Log.d("LONGCLICK", "✅ startActivity 호출 완료")
                } catch (e: Exception) {
                    Log.e("LONGCLICK", "❌ startActivity 실패: ${e.message}", e)
                }
            },
            onBookClick = { userBook -> showBookActionDialog(userBook) }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_actions, null)
        val input = dialogView.findViewById<EditText>(R.id.inputPage)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnSameAuthor = dialogView.findViewById<Button>(R.id.btnSameAuthor)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(this)
            .setTitle("📖 ${userBook.book?.title ?: "책 제목 없음"}")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnSave.setOnClickListener {
            val page = input.text.toString().toIntOrNull() ?: 0
            updateReadPage(userBook.id, page)
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deleteBook(userBook.id)
            dialog.dismiss()
        }

        btnSameAuthor.setOnClickListener {
            val authorName = userBook.book?.author
            if (!authorName.isNullOrBlank()) {
                val intent = Intent(this, SameAuthorActivity::class.java)
                intent.putExtra("AUTHOR_NAME", authorName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "저자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
        SupabaseClient.create().updateUserBookReadPageById("eq.$id", mapOf("read_page" to page))
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

                        // ✅ 중복 제거: 같은 ISBN이 여러 번 저장된 경우 하나만 유지
                        val distinctBooks = books.distinctBy { it.isbn }

                        userBookList.clear()
                        userBookList.addAll(distinctBooks)
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