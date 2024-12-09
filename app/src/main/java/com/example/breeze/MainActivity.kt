package com.example.breeze

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var myAdapter: MyAdapter
    lateinit var list: ArrayList<Data>
    lateinit var tempArraylist: ArrayList<Data>
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.isRefreshing = false
        recyclerView = findViewById(R.id.recyclerView)
        list = ArrayList()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val api = retrofitBuilder.create(ApiInterface::class.java)
        val retrofitData = api.getNews()

        retrofitData.enqueue(object : Callback<MyData?> {

            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                val responseBody = response.body()
                val products = responseBody?.data ?: emptyList<Data>()
                list.addAll(products) // Add products to the productList
                tempArraylist = ArrayList(list) // Copy productList to tempArraylist

                myAdapter = MyAdapter(this@MainActivity, tempArraylist)
                swipeRefreshLayout.setOnRefreshListener {
                    val refreshedData = responseBody?.data ?: emptyList<Data>()
                    list.clear()
                    list.addAll(refreshedData)
                    myAdapter.filterList(list)
                    swipeRefreshLayout.isRefreshing = false
                }
                myAdapter.setOnBookmarkClickListener { data ->
                    addBookmark(data)
                }

                val swipeGesture = object : SwipeGesture(this@MainActivity) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val fromPos = viewHolder.adapterPosition
                        val toPos = target.adapterPosition
                        Collections.swap(list, fromPos, toPos)
                        myAdapter.notifyItemMoved(fromPos, toPos)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        when (direction) {
                            ItemTouchHelper.LEFT -> {
                                myAdapter.deleteItem(viewHolder.adapterPosition)
                            }
                            ItemTouchHelper.RIGHT -> {
                                val x = list[viewHolder.adapterPosition]
                                myAdapter.deleteItem(viewHolder.adapterPosition)
                                myAdapter.addItem(list.size, x)
                            }
                        }
                    }
                }
                val touchHelper = ItemTouchHelper(swipeGesture)
                touchHelper.attachToRecyclerView(recyclerView)
                recyclerView.adapter = myAdapter
                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            }

            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Log.d("Main Activity ", "onFailure: " + t.message)
            }
        })
    }

    private fun addBookmark(data: Data) {
        //val database = FirebaseDatabase.getInstance()
        val auth=FirebaseAuth.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = userId?.let {
            FirebaseDatabase.getInstance().getReference("Bookmarks").child(
                it
            )
        }

        // Use Firebase's push() to generate a unique key for each bookmark
        val key = database?.push()?.key// Generates a unique ID automatically

        // Set the article as the value for this unique ID
        if (key != null) {
            database.child(key).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bookmark added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        val item = menu?.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView

        setupSearch(searchView)
        return true
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.lowercase() ?: ""
                if (searchText.isNotEmpty()) {
                    val filteredList = list.filter {
                        it.title.lowercase().contains(searchText)
                    }
                    tempArraylist.clear()
                    tempArraylist.addAll(filteredList)
                } else {
                    tempArraylist.clear()
                    tempArraylist.addAll(list)
                }
                myAdapter.filterList(tempArraylist)
                recyclerView.adapter!!.notifyDataSetChanged()
                return false
            }
        })
    }
}