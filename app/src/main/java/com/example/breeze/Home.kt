package com.example.breeze

import android.content.Intent
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class Home : Fragment(R.layout.home_fragment) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var list = ArrayList<Data>()
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(requireContext(), list)
        recyclerView.adapter = myAdapter
        progressBar = view.findViewById(R.id.progressBar)
        var fab =view.findViewById<FloatingActionButton>(R.id.fab)
        auth = FirebaseAuth.getInstance()
        fetchNews()
        fab.setOnClickListener(){
            startActivity(Intent(requireContext(),pfp::class.java))

        }
        swipeRefreshLayout.setOnRefreshListener {
            fetchNews()
        }

        myAdapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
            override fun onItemClicking(position: Int) {
                // on clicking each item , what action do you want to perform
                val intent = Intent(requireContext(), Webview::class.java)
                intent.putExtra("url", list[position].url)
                startActivity(intent)



            }

        })




    }

    private fun fetchNews() {
        progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiInterface::class.java)
        val call = api.getNews()

        call.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                logApiResponse(response)
                //Log.d("response", "$response")
                if (response.isSuccessful) {
                    val products = response.body()?.data ?: emptyList<Data>()
                    list.clear()
                    list.addAll(products)
                    myAdapter.notifyDataSetChanged()
                }
                progressBar.visibility = View.GONE

                swipeRefreshLayout.isRefreshing = false
                myAdapter.setOnBookmarkClickListener { data ->
                    addBookmark(data)
                }

                val swipeGesture = object : SwipeGesture(requireContext()) {
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
                            ItemTouchHelper.LEFT -> myAdapter.deleteItem(viewHolder.adapterPosition)
                            ItemTouchHelper.RIGHT -> {
                                val item = list[viewHolder.adapterPosition]
                                myAdapter.deleteItem(viewHolder.adapterPosition)
                                myAdapter.addItem(list.size, item)
                            }
                        }
                    }
                }
                val touchHelper = ItemTouchHelper(swipeGesture)
                touchHelper.attachToRecyclerView(recyclerView)
            }




            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Log.e("HomeFragment", "onFailure: ${t.message}")
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun logApiResponse(response: Response<MyData?>) {
        if (response.isSuccessful) {
            Log.d("API_RESPONSE", "Response Code: ${response.code()}")
            Log.d("API_RESPONSE", "Response Body: ${response.body().toString()}")
            Log.d("API_RESPONSE", "Headers: ${response.headers()}")
        } else {
            Log.e("API_RESPONSE", "Error Code: ${response.code()}")
            Log.e("API_RESPONSE", "Error Body: ${response.errorBody()?.string()}")
        }
    }

    private fun addBookmark(data: Data) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)
        val key = database.push().key
        if (key != null) {
            data.key=key
        }
        key?.let {
            database.child(it).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bookmark added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                }
        }
    }


}
