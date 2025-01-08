package com.google.ar.core.examples.kotlin.helloar.profile

data class VoiceDto(
    val memberId: Int,
    val name: String,
    val date: String,
    val id: String,
    var selected: Boolean
)