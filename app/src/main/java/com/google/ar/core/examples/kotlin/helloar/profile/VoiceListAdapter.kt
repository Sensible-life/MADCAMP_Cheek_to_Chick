package com.google.ar.core.examples.kotlin.helloar.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R

class VoiceListAdapter(private val context: Context,
    private var dataList: MutableList<VoiceDto>) : RecyclerView.Adapter<VoiceListAdapter.VoiceListViewHoler>() {


    // ViewHolder 클래스
    class VoiceListViewHoler(view: View) : RecyclerView.ViewHolder(view) {
        val voice_name: TextView = view.findViewById(R.id.voice_name)
        val made_date: TextView = view.findViewById(R.id.made_date)
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
        holder.made_date.text = data.date
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
