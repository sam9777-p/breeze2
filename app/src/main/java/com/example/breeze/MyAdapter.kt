package com.example.breeze

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class MyAdapter(val context: Context, var list: ArrayList<Data>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>(){
        var lastpos = -1;
    private var onBookmarkClickListener: ((Data) -> Unit)? = null
    fun setOnBookmarkClickListener(listener: (Data) -> Unit) {
        onBookmarkClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(position:Int){
        list.removeAt(position)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(position: Int, product: Data){
        list.add(position,product)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun filterList(list1: ArrayList<Data>){
        list = list1
        notifyDataSetChanged()
    }
    fun updateData(newList: ArrayList<Data>) {
        list.clear()
        list.addAll(newList)
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
        holder.excerpt.text = currentItem.excerpt
        Glide.with(holder.itemView.context)
            .load(currentItem.thumbnail)
            .into(holder.image)
        setanimation(holder.itemView, position)
        holder.bookmarkButton.setOnClickListener {
            onBookmarkClickListener?.invoke(currentItem)
        }

        //Picasso.get().load(currentItem.urlToImage).into(holder.image);
    }
    private fun setanimation(view: View, position: Int){
        if(position>lastpos){
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            view.startAnimation(animation)
            lastpos = position
        }

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var title : TextView
        var image : ShapeableImageView
        var excerpt : TextView
        var bookmarkButton: ImageButton
        init {
            title = itemView.findViewById(R.id.articleTitle)
            image = itemView.findViewById(R.id.articleImage)
            excerpt = itemView.findViewById(R.id.articleExcerpt)
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton)
        }

    }

}