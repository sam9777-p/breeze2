package com.example.breeze




data class Data(
    var key:String="",
    val authors: List<String> = emptyList(),

    val contentLength: Int = 0,

    val date: String = "",

    val excerpt: String = "",

    val keywords: List<String> = emptyList(),

    val language: String = "",

    val paywall: Boolean = false,

    val publisher: Publisher = Publisher(),

    val thumbnail: String = "",

    val title: String = "",

    val url: String = ""

)
