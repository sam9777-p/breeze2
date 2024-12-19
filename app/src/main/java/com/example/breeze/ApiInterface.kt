
package com.example.breeze
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "x-rapidapi-key:756dc2f204msh94e5ee29b5cce4dp19086ajsnb5ae69706c9c",
        "x-rapidapi-host: news-api14.p.rapidapi.com"
    )
    @GET("/v2/search/articles")
    fun getNews(
        @Query("query") topic: String= "Sports",
        @Query("language") language: String = "en"
    ): Call<MyData>
}
