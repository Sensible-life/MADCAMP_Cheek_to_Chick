import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ar.core.examples.kotlin.helloar.R
import com.mpackage.network.ListedBooks
import com.mpackage.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListedBookAdapter(
    private val books: List<ListedBooks>,
    private val onLikeClicked: (ListedBooks) -> Unit
) : RecyclerView.Adapter<ListedBookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleImage: ImageView = view.findViewById(R.id.titleImage)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val likeButton: ImageButton = view.findViewById(R.id.likeButton)
        val rankingText: TextView = itemView.findViewById(R.id.rankingText) // Add a ranking TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listed_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        // Bind data
        holder.titleText.text = "${position + 1}. ${book.title}" // Add item number
        Glide.with(holder.itemView.context)
            .load(book.title_img)
            .into(holder.titleImage)
        holder.rankingText.text = "Ranking: ${book.ranking}"

        // Change item color based on ranking
        if (book.ranking in 1..5) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFD700")) // Gold color for top 5
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF")) // Default white
        }

        // Title Image
        Glide.with(holder.itemView.context)
            .load(book.title_img)
            .into(holder.titleImage)

        // Title Text
        holder.titleText.text = book.title

        // Like Button
        updateLikeButton(holder.likeButton, book.likes)

//        holder.likeButton.setOnClickListener {
//            val updatedBook = book.copy(likes = !book.likes) // 새로운 객체 생성
//            updateLikeButton(holder.likeButton, updatedBook.likes)
//            onLikeClicked(updatedBook)
//        }





    }

    override fun getItemCount(): Int = books.size

    private fun updateLikeButton(button: ImageButton, isLiked: Boolean) {
        val icon = if (isLiked) R.drawable.icon_favorite_filled else R.drawable.icon_favourite
        button.setImageResource(icon)
    }
}
