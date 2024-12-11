
package com.example.breeze
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.breeze.Data
import com.example.breeze.MyData
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth


import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections
class Home : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private val newsList = ArrayList<Data>()
    private lateinit var list: ArrayList<Data>
    private lateinit var auth: FirebaseAuth
    //private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        //swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(requireContext(), newsList)
        recyclerView.adapter = myAdapter



        auth = FirebaseAuth.getInstance()
        return view
    }

    private fun fetchNews() {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(ApiInterface::class.java)
        val retrofitData = api.getNews()
        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                val responseBody = response.body()
                val products = responseBody?.data ?: emptyList<Data>()

                // Initialize list if not already done
                if (!::list.isInitialized) {
                    list = ArrayList()
                }

                list.addAll(products)
                newsList.addAll(list)

                myAdapter.notifyDataSetChanged()

                //swipeRefreshLayout.isRefreshing = false

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
                Log.d("HomeFragment", "onFailure: ${t.message}")
               // swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun addBookmark(data: Data) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)
        val key = database.push().key
        if (key != null) {
            database.child(key).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bookmark added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
