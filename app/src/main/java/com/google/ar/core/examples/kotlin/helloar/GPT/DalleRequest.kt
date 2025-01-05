package com.google.ar.core.examples.kotlin.helloar.GPT

data class DalleRequest(
    val prompt: String,
    val n: Int = 1,
    val size: String = "1024x1024"
)
