package com.example.readingbooks.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.readingbooks.R
import com.example.readingbooks.data.UserBook

class UserBookAdapter(
    private val books: List<UserBook>,
    private val onBookLongClick: (String) -> Unit,
    private val onBookClick: (UserBook) -> Unit
) : RecyclerView.Adapter<UserBookAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val author = itemView.findViewById<TextView>(R.id.textAuthor)
        val textReadPage: TextView = view.findViewById(R.id.textReadPage)
        val thumbnail: ImageView = view.findViewById(R.id.imageThumbnail)
        
        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition // ← 변경
                if (position != RecyclerView.NO_POSITION) {
                    onBookClick(books[position])
                }
            }

            view.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val userBook = books[position]
                    val authorName = userBook.book.author
                    val bookTitle = userBook.book.title
                    
                    Log.d("롱클릭", "롱클릭 동작")
                    Log.d("롱클릭", "AuthorName: '$authorName'")
                    Log.d("롱클릭", "BookTitle: '$bookTitle'")

                    when {
                        // 1️⃣ 저자 정보가 있으면 바로 검색
                        !authorName.isNullOrBlank() -> {
                            try {
                                onBookLongClick(authorName)
                            } catch (e: Exception) {
                                Log.e("롱클릭", "❌ startActivity 실패: ${e.message}", e)
                            }
                        }
                        // 2️⃣ 저자 정보가 없으면 책 제목으로 검색
                        !bookTitle.isNullOrBlank() -> {
                            try {
                                Log.d("롱클릭", "✅ 책 제목으로 검색: $bookTitle")
                                onBookLongClick(bookTitle) // 제목으로 검색
                            } catch (e: Exception) {
                                Log.e("롱클릭", "❌ startActivity 실패: ${e.message}", e)
                            }
                        }
                        // 3️⃣ 둘 다 없으면 에러
                        else -> {
                            Log.e("롱클릭", "❌ 저자와 제목 모두 비어있습니다")
                            Toast.makeText(itemView.context, "책 정보가 부족합니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val book = books[position]

        Log.d("UserBookAdapter", "book.title=${book.book.title}, author=${book.book.author}, page=${book.read_page}")

        holder.textTitle.text = "제목: ${book.book.title ?: "제목 없음"}"
        holder.author.text = "저자: ${book.book.author ?: "알 수 없음"}"
        holder.textReadPage.text = "읽은 페이지: ${book.read_page ?: 0}쪽"
        holder.thumbnail.load(book.book.thumbnail)

    }


    override fun getItemCount(): Int = books.size
}