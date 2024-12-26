package com.example.breeze

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Home : Fragment(R.layout.home_fragment) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerAdapter: ShimmerAdapter
    private lateinit var myAdapter: MyAdapter
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var list = ArrayList<Data>()
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        shimmerFrameLayout = view.findViewById(R.id.shimmerFrameLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        auth = FirebaseAuth.getInstance()

        // Initialize adapters
        shimmerAdapter = ShimmerAdapter(requireContext())
        myAdapter = MyAdapter(requireContext(), list)

        // Set shimmer adapter initially
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = shimmerAdapter

        auth.currentUser?.let { user ->
            val overlappingIcon = view.findViewById<ImageView>(R.id.overlapping_icon)
            overlappingIcon?.let { loadProfilePicture(user.uid, it) }
        }

        fetchNews()

        fab.setOnClickListener {
            startActivity(Intent(requireContext(), pfp::class.java))
        }

        swipeRefreshLayout.setOnRefreshListener {
            fetchNews()
        }

        myAdapter.setOnBookmarkToggleListener { data, isBookmarked ->
            if (isBookmarked) addBookmark(data) else removeBookmark(data)
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
        // Start shimmer effect and set shimmer adapter
        shimmerFrameLayout.startShimmer()
        recyclerView.adapter = shimmerAdapter
        shimmerFrameLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiInterface::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("HomeFragment", "Fetching news...")

                if (!isNetworkAvailable()) {
                    Log.e("HomeFragment", "No internet connection")
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Fetch news from API
                val response = withContext(Dispatchers.IO) { api.getNews() }
                Log.d("HomeFragment", "API Response: $response")

                if (response.data != null) {
                    // Log the fetched data to confirm it's not null
                    Log.d("HomeFragment", "Fetched news data size: ${response.data!!.size}")

                    // Update the news list and sync bookmarks
                    list.clear()
                    list.addAll(response.data!!)
                    fetchBookmarksAndSync()
                } else {
                    Log.e("HomeFragment", "No data in response")
                    Toast.makeText(requireContext(), "No news available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching news: ${e.message}")
                Toast.makeText(requireContext(), "Failed to fetch news: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // Stop shimmer and switch to the main adapter
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                // Check if the list has been populated with data
                Log.d("HomeFragment", "List size after fetching: ${list.size}")

                // Update adapter data
                myAdapter.updateData(list)
                myAdapter.notifyDataSetChanged()

                // Check adapter and RecyclerView visibility
                Log.d("HomeFragment", "Adapter updated and RecyclerView visible")

                swipeRefreshLayout.isRefreshing = false
            }
        }
    }




    private fun addBookmark(data: Data) { /* Same as before */ }
    private fun removeBookmark(data: Data) { /* Same as before */ }
    private fun fetchBookmarksAndSync() { /* Same as before */ }

    private fun loadProfilePicture(userId: String, profileImageView: ImageView) {
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val profilePicUri = sharedPreferences.getString("${userId}_profilePicUri", null)

        profilePicUri?.let {
            Glide.with(this)
                .load(Uri.parse(it))
                .placeholder(R.drawable.baseline_person_24)
                .circleCrop()
                .into(profileImageView)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(android.net.ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}
