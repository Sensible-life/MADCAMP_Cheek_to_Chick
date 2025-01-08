package com.google.ar.core.examples.kotlin.helloar.profile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R

class VoiceListAdapter(private val context: Context,
    private var dataList: MutableList<VoiceDto>) : RecyclerView.Adapter<VoiceListAdapter.VoiceListViewHoler>() {


    // ViewHolder 클래스
    class VoiceListViewHoler(view: View) : RecyclerView.ViewHolder(view) {
        val voice_name: TextView = view.findViewById(R.id.voice_name)
    }
    // ViewHolder를 생성하는 부분
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceListViewHoler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.screen_profile_voice_list_content, parent, false)
        return VoiceListViewHoler(view)
    }

    // 각 아이템을 바인딩하는 부분
    override fun onBindViewHolder(holder: VoiceListViewHoler, position: Int) {
        val data = dataList[position]
        holder.voice_name.text = data.name


        val onButton = holder.itemView.findViewById<FrameLayout>(R.id.selectButton)
        if (data.selected) {
            onButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow))
        }
        else {
            onButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.lightBlack))
        }

        onButton.setOnClickListener {
            data.selected = true

            // 나머지 항목들을 false로 설정
            for (item in dataList) {
                if (item != data) {
                    item.selected = false
                }
            }
            notifyDataSetChanged()
            SelectedVoiceData.clearVoiceData()
            SelectedVoiceData.addVoiceItem(VoiceDto(1, data.name, data.date, data.id, data.selected))
            val name = data.name
            Log.d("DEBUG", "Voice has been changed to $name")
        }
    }


    override fun getItemCount(): Int {
        return dataList.size
    }
}
