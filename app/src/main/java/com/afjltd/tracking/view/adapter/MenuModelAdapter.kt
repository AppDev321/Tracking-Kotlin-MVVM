package com.afjltd.tracking.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.model.responses.VehicleMenu
import com.bumptech.glide.Glide
import com.afjltd.tracking.R


class MenuModelAdapter (private val context: Context,private val menuList: List<VehicleMenu>,private val listner: MenuItemListner)
        : RecyclerView.Adapter<MenuModelAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int): ViewHolder {
            // inflate the custom view from xml layout file
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.menu_item,parent,false)

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

             val iconLottie = itemView.findViewById<ImageView>(R.id.iconImageView)
             val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
            val cardItem = itemView.findViewById<CardView>(R.id.cardItem)


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
