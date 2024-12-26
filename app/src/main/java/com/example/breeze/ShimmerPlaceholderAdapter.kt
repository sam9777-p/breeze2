package com.example.breeze

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ShimmerAdapter(private val context: Context) : RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.shimmer_item, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // No binding needed, shimmer layout handles animation
    }

    override fun getItemCount(): Int = 10 // Show 10 shimmer items by default

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
