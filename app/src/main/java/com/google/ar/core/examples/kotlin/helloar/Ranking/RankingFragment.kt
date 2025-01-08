package com.google.ar.core.examples.kotlin.helloar.Ranking

import ListedBookAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mpackage.GridBookAdapter
import com.mpackage.network.LikedBooks
import com.mpackage.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RankingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var books: List<LikedBooks> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ranking_fragment, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.gridRecyclerview)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5) // 2 columns
        recyclerView.setHasFixedSize(true)

        // Fetch and display books
        fetchBooks()

    }

    private fun fetchBooks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.listedBooksApi.getAllBooks()
                if (response.isSuccessful) {
                    books = response.body()?.sortedBy { it.ranking } ?: emptyList()

                    withContext(Dispatchers.Main) {
                        // Update RecyclerView
                        recyclerView.adapter = GridBookAdapter(books)
                        Toast.makeText(requireContext(), "Fetched ${books.size} books", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch books: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}