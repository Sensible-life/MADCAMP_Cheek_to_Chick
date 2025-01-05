package com.google.ar.core.examples.kotlin.helloar.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.R
import com.mpackage.network.ApiService



class ProfileFragment : Fragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.screen_profile, container, false)
    }
}