
package com.example.breeze
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.breeze.Data
import com.example.breeze.MyData
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ProgressBar

import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Collections



class Bookmarks : Fragment(R.layout.bookmarks_fragment) {
    // private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    // private val newsList = ArrayList<Data>()
    private var list= ArrayList<Data>()
    private lateinit var auth: FirebaseAuth
    private val bookmarkedList = ArrayList<Data>()
    private lateinit var progressBar: ProgressBar
    //private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //val view = inflater.inflate(, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        //swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(requireContext(), list)
        recyclerView.adapter = myAdapter
        progressBar=view.findViewById(R.id.Pg_bar)
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        myAdapter.setOnBookmarkToggleListener { data, isBookmarked ->
            if (!isBookmarked) {
                removeItemFromFirebase(data)
            }
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId)
        progressBar.visibility = View.VISIBLE

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookmarkList = ArrayList<Data>()
                // Loop through all bookmarks

                for (bookmarkSnapshot in snapshot.children) {
                    val data = bookmarkSnapshot.getValue(Data::class.java)
                    if (data != null) {
                        bookmarkList.add(data)
                    }
                }
                myAdapter.updateData(bookmarkList)
                progressBar.visibility = View.GONE

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load bookmarks: ${error.message}",
                    Toast.LENGTH_SHORT

                ).show()
            }

        })


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
                        val position = viewHolder.adapterPosition
                        //val itemKey = list[position].key
                        removeItemFromFirebase(list[position])
                        //myAdapter.deleteItem(viewHolder.adapterPosition)
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

        myAdapter.setOnItemClickListener(object : MyAdapter.onItemClickListener {
            override fun onItemClicking(position: Int) {
                // on clicking each item , what action do you want to perform
                val intent = Intent(requireContext(), Webview::class.java)

                intent.putExtra("url", list[position].url)
                startActivity(intent)


            }

        })


    }
    private fun removeItemFromFirebase(data: Data) {
        val itemKey=data.key
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Bookmarks").child(userId).child(itemKey)

        databaseRef.removeValue()
            .addOnSuccessListener {
                list.remove(data)
                myAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Item removed successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to remove item: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



}