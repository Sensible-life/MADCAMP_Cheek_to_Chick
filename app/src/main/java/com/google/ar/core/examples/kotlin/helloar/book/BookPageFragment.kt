package com.google.ar.core.examples.kotlin.helloar.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.ar.core.examples.kotlin.helloar.R

class BookPageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_book_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageContent = arguments?.getString("content")
        val pageImageUrl = arguments?.getString("imageUrl")

        val textView = view.findViewById<TextView>(R.id.page_text)
        val imageView = view.findViewById<ImageView>(R.id.page_image)

        textView.text = pageContent
        context?.let {
            Glide.with(it).load(pageImageUrl).into(imageView)
        }
    }

    companion object {
        fun newInstance(content: String, imageUrl: String): BookPageFragment {
            val fragment = BookPageFragment()
            val args = Bundle()
            args.putString("content", content)
            args.putString("imageUrl", imageUrl)
            fragment.arguments = args
            return fragment
        }
    }
}
