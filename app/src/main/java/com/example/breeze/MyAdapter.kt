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

class MyAdapter(val context: Context, var list: ArrayList<Data>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>(){

    private var onBookmarkToggleListener: ((Data, Boolean) -> Unit)? = null
    fun setOnBookmarkToggleListener(listener3: (Data, Boolean) -> Unit) {
        onBookmarkToggleListener = listener3
    }


    private lateinit var myListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClicking(position: Int)
    }

    fun setOnItemClickListener(listener2 : onItemClickListener){
        myListener = listener2
    }

    var lastpos = -1;



    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(position:Int){
        list.removeAt(position)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(position: Int, product: Data){
        list.add(position,product)
        notifyDataSetChanged()
    }

    fun updateData(newList: ArrayList<Data>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.eachitem, parent, false)
        return MyViewHolder(itemView,myListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = list[position]
        holder.title.text = currentItem.title
        holder.excerpt.text = currentItem.excerpt
        holder.source.text="Source: ${currentItem.publisher.name} "
        holder.tag.text=currentItem.tag
        Glide.with(holder.itemView.context)
            .load(currentItem.thumbnail)
            .into(holder.image)

        holder.bookmarkButton.setImageResource(
            if (currentItem.isBookmarked) R.drawable.ic_bookmark else R.drawable.baseline_bookmark_border_24
        )

        setanimation(holder.itemView, position)
        holder.bookmarkButton.setOnClickListener {
            currentItem.isBookmarked = !currentItem.isBookmarked
            onBookmarkToggleListener?.invoke(currentItem, currentItem.isBookmarked)
            notifyItemChanged(position)
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

    class MyViewHolder(itemView : View,listener2: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        var title : TextView
        var image : ShapeableImageView
        var excerpt : TextView
        var bookmarkButton: ImageButton
        var source:TextView
        var tag:TextView

        init {
            title = itemView.findViewById(R.id.articleTitle)
            image = itemView.findViewById(R.id.articleImage)
            excerpt = itemView.findViewById(R.id.articleExcerpt)
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton)
            source = itemView.findViewById(R.id.articleSource)
            tag=itemView.findViewById(R.id.newsTag)
            itemView.setOnClickListener {
                listener2.onItemClicking(adapterPosition)
            }
        }

    }

}
