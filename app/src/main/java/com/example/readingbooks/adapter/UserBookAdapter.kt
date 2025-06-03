package com.example.readingbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.readingbooks.R
import com.example.readingbooks.data.UserBook

class UserBookAdapter(private val books: List<UserBook>) :
    RecyclerView.Adapter<UserBookAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textReview: TextView = view.findViewById(R.id.textReview)
        val textReadPage: TextView = view.findViewById(R.id.textReadPage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.textTitle.text = "제목: ${book.isbn}"
        holder.textReview.text = book.review ?: "리뷰 없음"
        holder.textReadPage.text = "읽은 페이지: ${book.read_page}쪽"
        
    }

    override fun getItemCount(): Int = books.size
}
