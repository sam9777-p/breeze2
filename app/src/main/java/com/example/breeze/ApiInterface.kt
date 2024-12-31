
package com.example.breeze

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "x-rapidapi-key:a123d43e74mshead248ff5ed7b70p1eea2djsnf086cf973dab",
        "x-rapidapi-host: news-api14.p.rapidapi.com"
    )
    @GET("/v2/search/articles")
    suspend fun getNews(
        @Query("query") topic: String= "Trending News",
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1
    ): MyData
}
