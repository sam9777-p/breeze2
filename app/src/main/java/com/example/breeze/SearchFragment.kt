package com.example.breeze
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
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


class SearchFragment : Fragment(R.layout.search_fragment) {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    //private val newsList = ArrayList<Data>()
    var list= ArrayList<Data>()
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //val view = inflater.inflate(R.layout.search_fragment, container, false)
        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView.layoutManager=LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(requireContext(),list)
        recyclerView.adapter = myAdapter
        progressBar=view.findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()
        fetchNews("General")

        myAdapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
            override fun onItemClicking(position: Int) {

                val intent = Intent(requireContext(), Webview::class.java)
                intent.putExtra("url", list[position].url)
                startActivity(intent)


            }

        })
        setupSearchListener()

        //return view
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    fetchNews(it)
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun fetchNews(query: String) {
        progressBar.visibility = View.VISIBLE
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(ApiInterface::class.java)
        val retrofitData = api.getNews(topic=query)
        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {

                val responseBody = response.body()
                val products = responseBody?.data ?: emptyList<Data>()
                list.clear()
                list.addAll(products)
                //list.addAll(list)

                myAdapter = MyAdapter(requireContext(), list)
                recyclerView.adapter = myAdapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())

                myAdapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
                    override fun onItemClicking(position: Int) {
                        val intent = Intent(requireContext(), Webview::class.java)
                        intent.putExtra("url", list[position].url)
                        startActivity(intent)
                    }
                })

                progressBar.visibility = View.GONE

                /*swipeRefreshLayout.setOnRefreshListener {
                    list.clear()
                    list.addAll(products)
                    myAdapter.filterList(list)
                    swipeRefreshLayout.isRefreshing = false
                }*/

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
                            ItemTouchHelper.LEFT -> {

                                myAdapter.deleteItem(viewHolder.adapterPosition)

                            }
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
                Log.d("MainActivity", "onFailure: ${t.message}")
            }
        })
    }
    private fun addBookmark(data: Data) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)
        val key = database.push().key
        if (key != null) {
            data.key=key
        }
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