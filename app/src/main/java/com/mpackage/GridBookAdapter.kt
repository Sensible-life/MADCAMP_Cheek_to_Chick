package com.mpackage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R
import com.mpackage.network.LikedBooks

class GridBookAdapter(
    private val books: List<LikedBooks>

) : RecyclerView.Adapter<GridBookAdapter.GridBookViewHolder>() {

    // 정렬된 리스트를 별도로 생성
    //private val sortedBooks = books.sortedBy { it.ranking }

    // 정렬된 리스트를 별도로 생성
    private val sortedBooks = books.map { book ->
        book.copy(ranking = (1..50).random())
    }.sortedBy { it.ranking }



    class GridBookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleImage: ImageView = view.findViewById(R.id.titleImage)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val likeCount: TextView = view.findViewById(R.id.likeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_listed_bookitem, parent, false)
        return GridBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridBookViewHolder, position: Int) {
        val book = sortedBooks[position]

        // Set title
        holder.titleText.text = book.title

        // Set ranking
        holder.likeCount.text = "${book.ranking}"

        // Decode and set image
        val decodedImage = decodeBase64Image(book.title_img)
        if (decodedImage != null) {
            holder.titleImage.setImageBitmap(decodedImage)
        } else {
            holder.titleImage.setImageResource(R.drawable.default_image) // Fallback image
        }

//        // Highlight top rankings
//        if (book.ranking in 1..5) {
//            holder.itemView.setBackgroundColor(Color.parseColor("#FFD700")) // Gold color
//        } else {
//            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF")) // Default white
//        }
    }

    override fun getItemCount(): Int = sortedBooks.size

    private fun decodeBase64Image(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
