package com.example.breeze

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


abstract class SwipeGesture(context: Context) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT ) {
    val deleteColor = ContextCompat.getColor(context, R.color.deleteColor)
    val archiveColor = ContextCompat.getColor(context, R.color.archiveColor)
    val deleteIcon = R.drawable.img1
    val archiveIcon = R.drawable.img2



    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    )
    {
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addSwipeLeftBackgroundColor(deleteColor)
            .addSwipeRightBackgroundColor(archiveColor)
            .addSwipeLeftActionIcon(deleteIcon)
            .addSwipeRightActionIcon(archiveIcon)
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}

