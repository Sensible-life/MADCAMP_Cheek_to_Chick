package com.google.ar.core.examples.kotlin.helloar.profile

object SelectedVoiceData {
    private val voiceList: MutableList<VoiceDto> = mutableListOf(
    )

    fun getVoiceDataList(): MutableList<VoiceDto> {
        return voiceList
    }

    fun addVoiceItem(item: VoiceDto) {
        voiceList.add(item)
    }

    fun clearVoiceData() {
        voiceList.clear()
    }
}