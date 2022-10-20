package com.example.afjtracking.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.bumptech.glide.Glide
import com.example.afjtracking.R
import com.example.afjtracking.model.responses.VehicleMenu


class MenuModelAdapter (private val context: Context,private val menuList: List<VehicleMenu>,private val listner: MenuItemListner)
        : RecyclerView.Adapter<MenuModelAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int): ViewHolder {
            // inflate the custom view from xml layout file
            val view: View = LayoutInflater.from(parent.context)
                .inflate(com.example.afjtracking.R.layout.menu_item,parent,false)

            // return the view holder
            return ViewHolder(view)

        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
           val menuModel = menuList[position]
            holder.titleTextView.text = menuModel.name

           // holder.iconLottie.setImageResource(menuModel.lottieJson)
            Glide.with(context)
                .load(menuModel.icon)
                .placeholder(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_no_image
                    )
                )

                .into(holder.iconLottie)


            val colorsTxt: Array<String> =
                context.resources
                    .getStringArray(R.array.color_white_text)

          //  holder.cardItem.setCardBackgroundColor(Color.parseColor(colorsTxt[position]))

            holder.itemView.setOnClickListener{
                listner.onMenuItemClick(menuModel)
            }
        }


        override fun getItemCount(): Int {
            // number of items in the data set held by the adapter
            return menuList.size
        }


        class ViewHolder(itemView: View)
            : RecyclerView.ViewHolder(itemView){

             val iconLottie = itemView.findViewById<ImageView>(com.example.afjtracking.R.id.iconImageView)
             val titleTextView = itemView.findViewById<TextView>(com.example.afjtracking.R.id.titleTextView)
            val cardItem = itemView.findViewById<CardView>(com.example.afjtracking.R.id.cardItem)


        }


        // this two methods useful for avoiding duplicate item
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }


        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

interface MenuItemListner
{
        fun onMenuItemClick(item:VehicleMenu)
}
