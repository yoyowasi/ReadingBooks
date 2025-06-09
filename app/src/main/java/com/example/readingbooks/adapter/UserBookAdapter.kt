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
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookClick(books[position])
                }
            }

            view.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val authorName = books[position].book.author
                    Log.d("롱클릭", "롱클릭 동작, authorName=$authorName")

                    if (!authorName.isNullOrBlank()) {
                        try {
                            onBookLongClick(authorName)
                        } catch (e: Exception) {
                            Log.e("롱클릭", "startActivity 실패: ${e.message}", e)
                        }
                    } else {
                        Log.e("롱클릭", "❌ authorName이 null 또는 빈 문자열입니다.")
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