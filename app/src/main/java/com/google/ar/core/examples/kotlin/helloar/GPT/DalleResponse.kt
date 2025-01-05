package com.google.ar.core.examples.kotlin.helloar.GPT

data class DalleResponse(
    val created: Long,
    val data: List<ImageData>
)

data class ImageData(
    val url: String
)

