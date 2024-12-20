
package com.example.breeze
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "x-rapidapi-key:a4713e9787mshd5899a37cc244d7p11b6fajsn9afc38f9e3cc",
        "x-rapidapi-host: news-api14.p.rapidapi.com"
    )
    @GET("/v2/search/articles")
    fun getNews(
        @Query("query") topic: String= "Sports",
        @Query("language") language: String = "en"
    ): Call<MyData>
}
