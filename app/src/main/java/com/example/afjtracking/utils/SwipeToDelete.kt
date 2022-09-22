package com.example.afjtracking.utils


import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R


class SwipeToDelete(val listener: SwipeListener) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.START or ItemTouchHelper.END,
    ItemTouchHelper.LEFT
) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        /* val adapter = recyclerView.adapter as MyAdapter
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition
        adapter.notifyItemMoved(from, to)
        return true*/
        listener.onMove(recyclerView, viewHolder, target)
        return false
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val background =
            ColorDrawable(viewHolder.itemView.context.resources.getColor(R.color.colorPrimary))

        val itemView: View = viewHolder.itemView
        if (dX > 0) {
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
        } else if (dX < 0) {
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
        } else {
            background.setBounds(0, 0, 0, 0)
        }
        // background.draw(c)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        var pos = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> {
                listener.onSwiped(pos)
            }
        }

    }


}

interface SwipeListener {
    fun onSwiped(pos: Int)
    fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) {
        /* val adapter = recyclerView.adapter as MyAdapter
         val from = viewHolder.adapterPosition
         val to = target.adapterPosition
         adapter.notifyItemMoved(from, to)*/
    }

}