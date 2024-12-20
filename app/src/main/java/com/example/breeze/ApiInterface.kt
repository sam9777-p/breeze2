
package com.example.breeze
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers(
        "x-rapidapi-key:ebebce4ca8msh1022a37d8630962p19f74ajsn382b09e49349",
        "x-rapidapi-host: news-api14.p.rapidapi.com"
    )
    @GET("/v2/search/articles")
    fun getNews(
        @Query("query") topic: String= "Sports",
        @Query("language") language: String = "en"
    ): Call<MyData>
}
