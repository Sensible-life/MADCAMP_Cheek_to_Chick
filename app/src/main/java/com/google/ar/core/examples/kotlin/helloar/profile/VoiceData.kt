package com.google.ar.core.examples.kotlin.helloar.profile

object VoiceData {
    private val voiceList: MutableList<VoiceDto> = mutableListOf(
        VoiceDto(1, "Sungwon", "2025-01-08", "fTFTil72L5XZs6FNuoZU", false)
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