package com.google.ar.core.examples.kotlin.helloar.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.R
import java.io.File
import java.io.IOException


@Suppress("DEPRECATION")
class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val voiceRecordLayout = view.findViewById<LinearLayout>(R.id.voice_record)

        // 클릭 이벤트
        voiceRecordLayout.setOnClickListener {
            // RecordActivity로 이동
            val intent = Intent(requireContext(), RecordActivity::class.java)
            startActivity(intent)
        }
    }
}
