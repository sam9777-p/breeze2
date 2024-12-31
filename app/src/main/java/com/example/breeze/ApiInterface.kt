
package com.example.breeze

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "x-rapidapi-key:8693b24f1emsh90fffe384f7064cp1ab6edjsn2303696c045b",
        "x-rapidapi-host: news-api14.p.rapidapi.com"
    )
    @GET("/v2/search/articles")
    suspend fun getNews(
        @Query("query") topic: String= "Trending News",
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1
    ): MyData
}
