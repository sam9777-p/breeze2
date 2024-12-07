package com.example.breeze

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class MyAdapter(val context : Activity, val list : ArrayList<Article>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>(){

    fun deleteitem(position:Int){
        list.removeAt(position)
        notifyDataSetChanged()
    }
    fun additem(position: Int,product: Article){
        list.add(position,product)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.eachitem, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = list[position]
        holder.title.text = currentItem.title
        Glide.with(context)
            .load(currentItem.urlToImage)
            .into(holder.image)

        //Picasso.get().load(currentItem.urlToImage).into(holder.image);
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var title : TextView
        var image : ShapeableImageView

        init {
            title = itemView.findViewById(R.id.articleTitle)
            image = itemView.findViewById(R.id.articleImage)
        }
    }

}