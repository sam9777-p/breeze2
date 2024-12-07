package com.example.breeze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var myAdapter: MyAdapter
    lateinit var list: ArrayList<Article>
    lateinit var tempArraylist: ArrayList<Article>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        list = ArrayList()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
        val retrofitData = retrofitBuilder.getTopHeadlines("us", "5a08d55b4e024420a7f6a7658c27a8c4")

        retrofitData.enqueue(object : Callback<MyData?> {

            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val productList = responseBody.articles

                    tempArraylist = ArrayList(productList)
                    myAdapter = MyAdapter(this@MainActivity, tempArraylist)

                    recyclerView.adapter = myAdapter
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

                    // Setup swipe gestures here...
                } else {
                    Log.e("API_ERROR", "Response unsuccessful or body is null")
                }
            }


            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                // if api call fails
                Log.d("Main Activity ", "onFailure: " + t.message)
            }
        })
    }
}