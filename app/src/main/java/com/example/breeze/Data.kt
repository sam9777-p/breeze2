package com.example.breeze

data class Data(
    val authors: List<String>,
    val contentLength: Int,
    val date: String,
    val excerpt: String,
    val keywords: List<String>,
    val language: String,
    val paywall: Boolean,
    val publisher: Publisher,
    val thumbnail: String,
    val title: String,
    val url: String
)