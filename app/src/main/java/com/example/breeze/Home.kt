package com.example.breeze

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Home : Fragment(R.layout.home_fragment) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val list = ArrayList<Data>()
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private var page = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        recyclerView = view.findViewById(R.id.recyclerView)
        val overlappingIcon = view.findViewById<ImageView>(R.id.overlapping_icon)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(requireContext(), list)
        recyclerView.adapter = myAdapter

        auth.currentUser?.let { user ->
            overlappingIcon?.let {
                loadProfilePicture(user.uid, it)
            }
        }

        fetchNews()

        fab.setOnClickListener {
            startActivity(Intent(requireContext(), pfp::class.java))
        }

        swipeRefreshLayout.setOnRefreshListener {
            page = (1..20).random()
            fetchNews()
        }

        myAdapter.setOnBookmarkToggleListener { data, isBookmarked ->
            if (isBookmarked) {
                addBookmark(data)
            } else {
                removeBookmark(data)
            }
        }

        myAdapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
            override fun onItemClicking(position: Int) {
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

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (isAdded) {
                    val newsDeferred = async(Dispatchers.IO) { api.getNews(page = page) }
                    val news = newsDeferred.await()
                    list.clear()
                    list.addAll(news.data)
                    fetchBookmarksAndSync()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to fetch news: ${e.message}")
                Toast.makeText(requireContext(), "Failed to fetch news", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun fetchBookmarksAndSync() {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)

        database.get().addOnSuccessListener { snapshot ->
            val bookmarkedArticles = snapshot.children.mapNotNull {
                val data = it.getValue(Data::class.java)
                data?.apply { key = it.key.toString() }
            }

            for (article in list) {
                val bookmarkMatch = bookmarkedArticles.find { it.url == article.url }
                if (bookmarkMatch != null) {
                    article.isBookmarked = true
                    article.key = bookmarkMatch.key
                } else {
                    article.isBookmarked = false
                    article.key = null.toString()
                }
            }
            myAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Log.e("HomeFragment", "Failed to fetch bookmarks: ${it.message}")
        }
    }

    private fun addBookmark(data: Data) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)

        val key = database.push().key
        if (key != null) {
            data.key = key
            database.child(key).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bookmark added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun removeBookmark(data: Data) {
        val itemKey = data.key ?: return
        val userId = auth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId).child(itemKey)

        databaseRef.removeValue()
            .addOnSuccessListener {
                data.isBookmarked = false
                data.key = null.toString()
                myAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Bookmark removed successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        fetchBookmarksAndSync()
    }

    private fun loadProfilePicture(userId: String, profileImageView: ImageView) {
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val profilePicUri = sharedPreferences.getString("${userId}_profilePicUri", null)

        if (profilePicUri != null) {
            Glide.with(this)
                .load(Uri.parse(profilePicUri))
                .placeholder(R.drawable.baseline_person_24)
                .circleCrop()
                .into(profileImageView)
        }
    }
}
