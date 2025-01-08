package com.google.ar.core.examples.kotlin.helloar.profile

object VoiceData {
    private val voiceList: MutableList<VoiceDto> = mutableListOf(
        VoiceDto(1, "조성원", "2025-01-08", "fTFTil72L5XZs6FNuoZU", false),
        VoiceDto(1, "허지민", "2025-01-08", "FpJagWLmj4gNqTIVmtBf", false),
        VoiceDto(1, "조어진", "2025-01-08", "7s97DhXouGuUa6wL0G5D", false),
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