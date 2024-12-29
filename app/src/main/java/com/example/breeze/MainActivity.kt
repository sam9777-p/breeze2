package com.example.breeze

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import androidx.navigation.fragment.findNavController

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Collections



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        replaceWithFragment(Home())


        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceWithFragment(Home()) // Show Home Fragment
                R.id.search_action -> replaceWithFragment(SearchFragment()) // Show Search Fragment
                R.id.bookmarks -> replaceWithFragment(Bookmarks()) // Show Bookmarks Fragment
            }
            true
        }
    }

    private fun replaceWithFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }





}

        /*val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.isRefreshing = false

        recyclerView = findViewById(R.id.recyclerView)
        list = ArrayList()
        tempArraylist = ArrayList()

        auth = FirebaseAuth.getInstance()
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
                list.addAll(products)
                tempArraylist.addAll(list)

                myAdapter = MyAdapter(this@MainActivity, tempArraylist)
                recyclerView.adapter = myAdapter
                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

                swipeRefreshLayout.setOnRefreshListener {
                    list.clear()
                    list.addAll(products)
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
                Log.d("MainActivity", "onFailure: ${t.message}")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                updateToolbarLogo(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateToolbarLogo(bitmap: Bitmap) {
        val drawable = BitmapDrawable(resources, bitmap)
        supportActionBar?.setLogo(drawable)
        supportActionBar?.setDisplayUseLogoEnabled(true)
    }

    private fun addBookmark(data: Data) {
        val userId = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)
        val key = database.push().key
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.change_logo -> {
                //openImagePicker()
                true
            }
            R.id.search_action -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.lowercase() ?: ""
                tempArraylist.clear()
                if (searchText.isNotEmpty()) {
                    tempArraylist.addAll(list.filter {
                        it.title.lowercase().contains(searchText)
                    })
                } else {
                    tempArraylist.addAll(list)
                }
                myAdapter.filterList(tempArraylist)
                myAdapter.notifyDataSetChanged()
                return false
            }
        })
    }
}
*/