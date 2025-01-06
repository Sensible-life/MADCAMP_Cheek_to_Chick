package com.google.ar.core.examples.kotlin.helloar.community

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.login.LoginActivity
import org.w3c.dom.Text

class LoadingFragment : Fragment() {

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"

        // newInstance 메서드 추가
        fun newInstance(pageNumber: Int): LoadingFragment {
            return LoadingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE_NUMBER, pageNumber)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageNumber = arguments?.getInt(ARG_PAGE_NUMBER) ?: 0
        println("Loading page number: $pageNumber")

        val splashGif = view.findViewById<ImageView>(R.id.splashGif)
        val text = view.findViewById<TextView>(R.id.loadingText)
        if (pageNumber == 2)
            text.text = "캐릭터를\n생성하는 중이에요"
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(700, 700)
                    .skipMemoryCache(true)
            )
            .into(splashGif)
    }
}
