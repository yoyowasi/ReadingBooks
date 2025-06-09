package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
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
import java.text.SimpleDateFormat
import java.util.*

class MyLibraryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var btnSameAuthor: Button
    private lateinit var btnGoalSetting: Button
    private lateinit var textReadingProgress: TextView

    private lateinit var textTotalBooks: TextView
    private lateinit var textTotalPages: TextView
    private lateinit var textTopAuthor: TextView


    private lateinit var adapter: UserBookAdapter
    private val userBookList = mutableListOf<UserBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        recycler = findViewById(R.id.recyclerMyBooks)
        btnLogout = findViewById(R.id.btnLogout)
        btnSameAuthor = findViewById(R.id.btnSameAuthor)
        btnGoalSetting = findViewById(R.id.btnGoalSetting)
        textReadingProgress = findViewById(R.id.textReadingProgress)
        textTotalBooks = findViewById(R.id.textTotalBooks)
        textTotalPages = findViewById(R.id.textTotalPages)
        textTopAuthor = findViewById(R.id.textTopAuthor)


        // 날짜 확인하여 하루마다 초기화
        val prefs = getSharedPreferences("reading_goal", MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("lastResetDate", "")

        if (today != savedDate) {
            prefs.edit()
                .putInt("totalReadPages", 0)
                .putString("lastResetDate", today)
                .apply()
        }

        updateReadingProgressText()

        adapter = UserBookAdapter(
            userBookList,
            onBookLongClick = { authorName ->
                val intent = Intent(this, SameAuthorActivity::class.java)
                intent.putExtra("AUTHOR_NAME", authorName)
                startActivity(intent)
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

        btnGoalSetting.setOnClickListener {
            startActivity(Intent(this, GoalSettingActivity::class.java))
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
                        fetchBooks()
                    } else {
                        Log.e("❌SUPABASE", "삭제 실패: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("❌SUPABASE", "삭제 실패: ${t.message}")
                }
            })
    }

    private fun updateReadPage(id: String, page: Int) {
        SupabaseClient.create().updateUserBookReadPageById("eq.$id", mapOf("read_page" to page))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val prefs = getSharedPreferences("reading_goal", MODE_PRIVATE)
                        val dailyGoal = prefs.getInt("dailyPageGoal", 30)

                        val newTotal = prefs.getInt("totalReadPages", 0) + page
                        prefs.edit().putInt("totalReadPages", newTotal).apply()

                        if (newTotal >= dailyGoal) {
                            Toast.makeText(this@MyLibraryActivity, "🎉 오늘 목표 달성!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MyLibraryActivity, "{$page}쪽 추가 기록됨", Toast.LENGTH_SHORT).show()
                        }

                        updateReadingProgressText()
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

    private fun updateReadingProgressText() {
        val prefs = getSharedPreferences("reading_goal", MODE_PRIVATE)
        val dailyGoal = prefs.getInt("dailyPageGoal", 30)
        val totalRead = prefs.getInt("totalReadPages", 0)
        textReadingProgress.text = "오늘 목표 ${dailyGoal}쪽 중 ${totalRead}쪽 읽음"
    }


    private fun fetchBooks() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        SupabaseClient.create().getUserBooksByUserId("eq.$uid")
            .enqueue(object : Callback<List<UserBook>> {
                override fun onResponse(call: Call<List<UserBook>>, response: Response<List<UserBook>>) {
                    if (response.isSuccessful) {
                        val books = response.body() ?: emptyList()
                        val distinctBooks = books.distinctBy { it.isbn }

                        userBookList.clear()
                        userBookList.addAll(distinctBooks)
                        adapter.notifyDataSetChanged()
                        calculateReadingStats(distinctBooks)
                    } else {
                        Log.e("❌SUPABASE", "불러오기 실패: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<UserBook>>, t: Throwable) {
                    Log.e("❌SUPABASE", "네트워크 오류: ${t.message}")
                }
            })
    }

    private fun calculateReadingStats(userBooks: List<UserBook>) {
        val totalBooks = userBooks.size
        val totalPages = userBooks.map { it.read_page ?: 0 }.sum()


        val topAuthor = userBooks
            .mapNotNull { it.book?.author }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }

        val mostReadAuthor = topAuthor?.key ?: "없음"
        val mostReadCount = topAuthor?.value ?: 0

        textTotalBooks.text = "총 도서 수: $totalBooks"
        textTotalPages.text = "총 읽은 페이지 수: $totalPages"
        textTopAuthor.text = "가장 많이 읽은 저자: $mostReadAuthor ({$mostReadCount}권)"
    }

}
