package com.google.ar.core.examples.kotlin.helloar.profile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.LinkedList

class AudioWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val waveData = LinkedList<Float>()
    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    fun updateWaveData(newData: FloatArray) {
        waveData.clear()
        newData.forEach {
            waveData.add(it)
        }
        invalidate() // 화면 갱신
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (waveData.isEmpty()) return

        val centerY = height / 2f
        val widthStep = width / waveData.size.toFloat()
        var startX = 0f

        for (value in waveData) {
            val stopX = startX + widthStep
            val amplitude = value * (height / 2) // 데이터의 크기에 따라 높이 설정
            canvas.drawLine(startX, centerY - amplitude, stopX, centerY + amplitude, paint)
            startX = stopX
        }
    }
}
