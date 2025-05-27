package com.example.readingbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.readingbooks.R
import com.example.readingbooks.data.model.BookDocument

class SearchResultAdapter(
    private val books: List<BookDocument>,
    private val onItemClick: (BookDocument) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.textTitle)
        val author = itemView.findViewById<TextView>(R.id.textAuthor)
        val thumbnail = itemView.findViewById<ImageView>(R.id.imageThumbnail)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(books[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.authors.joinToString(", ")
        holder.thumbnail.load(book.thumbnail)
    }

    override fun getItemCount(): Int = books.size
}
