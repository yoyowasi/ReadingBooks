package com.example.readingbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.readingbooks.R
import com.example.readingbooks.data.UserBook

class UserBookAdapter(
    private val books: List<UserBook>,
    private val onBookClick: (UserBook) -> Unit
) : RecyclerView.Adapter<UserBookAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textReadPage: TextView = view.findViewById(R.id.textReadPage)
        val thumbnail: ImageView = view.findViewById(R.id.imageThumbnail)
        val textPageCount: TextView = view.findViewById(R.id.textPageCount)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookClick(books[position])
                }
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

        holder.textTitle.text = "제목: ${book.book.title}"
        holder.textReadPage.text = "읽은 페이지: ${book.read_page}쪽"
        holder.thumbnail.load(book.book.thumbnail)

        // ✅ 페이지 수 표시 추가
        val pageCountText = book.book.page_count?.toString() ?: "쪽수 정보 없음"
        holder.textPageCount.text = "총 페이지 수: $pageCountText"
    }

    override fun getItemCount(): Int = books.size
}
