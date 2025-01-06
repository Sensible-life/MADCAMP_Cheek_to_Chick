package com.google.ar.core.examples.kotlin.helloar.profile

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RecordActivity : AppCompatActivity() {

    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private lateinit var waveformView: AudioWaveView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_profile_record)

        waveformView = findViewById(R.id.waveform_view)
        val recordButton = findViewById<Button>(R.id.record_button)

        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
                recordButton.text = "Start Recording"
            } else {
                startRecording()
                recordButton.text = "Stop Recording"
            }
        }
    }

    private fun startRecording() {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording = true

        // 코루틴을 사용해 녹음 데이터를 주기적으로 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)
            while (isActive && isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    // 샘플 데이터를 파형 뷰로 전달
                    val normalizedData = buffer.map { it / Short.MAX_VALUE.toFloat() }.toFloatArray()
                    runOnUiThread {
                        waveformView.updateWaveData(normalizedData)
                    }
                }
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}
