package com.example.breeze

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections
import java.util.Locale

class SearchFragment : Fragment(R.layout.search_fragment) {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private val list = ArrayList<Data>()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar
    private var coroutineJob: Job? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var spinnerCategories: Spinner
    private val searchQueryFlow = MutableStateFlow<String>("")
    private lateinit var voiceSearchButton: ImageButton

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
        private const val REQUEST_MIC_PERMISSION = 101
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.recycler_view_search)
        progressBar = view.findViewById(R.id.progressBar)
        //spinnerCategories = view.findViewById(R.id.spinner_categories)
        auth = FirebaseAuth.getInstance()
        voiceSearchButton = view.findViewById(R.id.voice_search_button)

        voiceSearchButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_MIC_PERMISSION)
            } else {
                startVoiceSearch()
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(requireContext(), list)
        recyclerView.adapter = myAdapter

        fetchNews("General")
        setupTabLayout()
        setupSearchListener()
        setupAdapterListeners()

        setupSwipeGestures()
    }



    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { fetchNews(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQueryFlow.value = newText ?: "" // Emit new search query
                return false
            }
        })

        lifecycleScope.launch {
            searchQueryFlow
                .debounce(500) // Delay API calls by 500ms
                .filter { it.isNotEmpty() } // Ignore empty queries
                .distinctUntilChanged() // Only emit if query is different
                .collect { query ->
                    fetchNews(query)
                }
        }
    }


    private fun setupAdapterListeners() {
        // Handle item click for WebView redirection
        myAdapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
            override fun onItemClicking(position: Int) {
                val intent = Intent(requireContext(), Webview::class.java)
                intent.putExtra("url", list[position].url)
                startActivity(intent)
            }
        })

        // Handle bookmark toggling
        myAdapter.setOnBookmarkToggleListener { data, isBookmarked ->
            if (isBookmarked) {
                addBookmark(data)
            } else {
                removeBookmark(data)
            }
        }
    }

    private fun setupSwipeGestures() {
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


    private fun fetchNews(query: String) {
        // Check if fragment is still attached to avoid illegal state exceptions
        if (!isAdded) {
            Log.e("SearchFragment", "Fragment is not attached")
            return
        }

        progressBar.visibility = View.VISIBLE
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://news-api14.p.rapidapi.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofitBuilder.create(ApiInterface::class.java)
        coroutineJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Ensure fragment is attached before accessing context or UI elements
                if (isAdded) {
                    val response = withContext(Dispatchers.IO) { api.getNews(query) }

                    if (response.data != null) {
                        list.clear()
                        list.addAll(response.data)
                        fetchBookmarksAndSync(query)
                    } else {
                        Log.e("SearchFragment", "No data found in response")
                    }
                } else {
                    Log.e("SearchFragment", "Fragment is not attached to the activity")
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Failed to fetch news: ${e.message}")

                context?.let {
                    Toast.makeText(it, "Failed to fetch news", Toast.LENGTH_SHORT).show()
                }
            } finally {
                // Ensure progress bar visibility is updated only when fragment is attached
                if (isAdded) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }


    private fun addBookmark(data: Data) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)

        // Generate a unique key
        val key = database.push().key
        if (key != null) {
            data.key = key // Assign the generated key to the Data object
        }

        // Save the bookmark with the generated key
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


    private fun removeBookmark(data: Data) {
        val itemKey = data.key
        if (itemKey.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Unable to remove bookmark. Key is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId).child(itemKey)

        databaseRef.removeValue()
            .addOnSuccessListener {
                data.isBookmarked = false // Update the local data
                data.key = null.toString() // Clear the key locally
                myAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Bookmark removed successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to remove item: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchBookmarksAndSync(tag: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)

        database.get().addOnSuccessListener { snapshot ->
            val bookmarkedArticles = snapshot.children.mapNotNull {
                val data = it.getValue(Data::class.java)
                data?.key = it.key.toString() // Assign the key from Firebase to the Data object
                data
            }

            // Update bookmark status and keys in the list
            for (article in list) {
                if (tag != null) {
                    article.tag=tag
                }
                val bookmarkMatch = bookmarkedArticles.find { it.url == article.url }
                if (bookmarkMatch != null) {
                    article.isBookmarked = true
                    article.key = bookmarkMatch.key // Sync the correct key
                } else {
                    article.isBookmarked = false
                    article.key = null.toString() // Clear the key if not bookmarked
                }
            }

            myAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Log.e("HomeFragment", "Failed to fetch bookmarks: ${it.message}")
        }
    }

    private fun setupTabLayout() {
        val tabLayout = view?.findViewById<TabLayout>(R.id.tab_layout)
        val categories = listOf("General", "Technology", "Sports", "Politics")

        for (category in categories) {
            tabLayout?.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.text?.let { fetchNews(it.toString()) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel the coroutine when fragment's view is destroyed
        coroutineJob?.cancel()
    }

    private val voiceSearchLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val speechResult = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = speechResult?.get(0) ?: ""

                // Set query in SearchView
                searchView.setQuery(spokenText, true)
            }
        }

    private fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search")
        }

        try {
            voiceSearchLauncher.launch(intent) // Use voiceSearchLauncher here
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Speech Recognition not supported!", Toast.LENGTH_SHORT).show()
        }
    }



}
