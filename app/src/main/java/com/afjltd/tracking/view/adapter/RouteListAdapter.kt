package com.afjltd.tracking.view.adapter


import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.ItemRouteListBinding
import com.afjltd.tracking.model.responses.Sheets
import com.afjltd.tracking.utils.PaginatedAdapter


class RouteListAdapter(

    private val mContext: AppCompatActivity
) : PaginatedAdapter<Sheets, RouteListAdapter.RouteListItem>() {


    lateinit var listners: ClickListenerInterface


    fun setListnerClick(listener: ClickListenerInterface) {
        this.listners = listener
    }

    override fun createCustomViewHolder(parent: ViewGroup, viewType: Int): RouteListItem {
        val itemView = DataBindingUtil.inflate(
            LayoutInflater.from(mContext), R.layout.item_route_list,
            parent, false
        ) as ItemRouteListBinding
        return RouteListItem(itemView)
    }


    override fun customBindHolder(holder: RouteListItem, position: Int) {

        val data = getItem(position)
        holder.itemRouteList.routSheet = data
        holder.itemRouteList.btnAction.setOnClickListener {
            data.isChildAbsent = 0
            listners.handleContinueButtonClick(data)
        }
        holder.itemRouteList.btnAbsent.setOnClickListener {
            data.isChildAbsent = 1
            listners.handleContinueButtonClick(data)
        }

        if (data.visibility == true) {
            val anim = ValueAnimator.ofFloat(1.0f, 0.95f)
            anim.duration = 1000
            anim.addUpdateListener { animation ->
                holder.itemView.scaleX = animation.animatedValue as Float
                holder.itemView.scaleY = animation.animatedValue as Float
            }
            anim.repeatCount = Animation.INFINITE
            anim.repeatMode = ValueAnimator.REVERSE
            anim.start()

            val alphaAnimation = AlphaAnimation(1.0f, 0.4f)
            alphaAnimation.duration = 1000
            alphaAnimation.repeatCount = Animation.INFINITE
            alphaAnimation.repeatMode = Animation.REVERSE
            holder.itemView.startAnimation(alphaAnimation)

            holder.itemView.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.blue_light))

        }

    }


    inner class RouteListItem(val itemRouteList: ItemRouteListBinding) :
        RecyclerView.ViewHolder(itemRouteList.root)


}