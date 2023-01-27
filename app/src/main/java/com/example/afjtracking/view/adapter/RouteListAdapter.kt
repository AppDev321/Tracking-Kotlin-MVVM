package com.example.afjtracking.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.databinding.ItemRouteListBinding
import com.example.afjtracking.databinding.LayoutWeeklyInspectionItemBinding
import com.example.afjtracking.model.responses.Sheets
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.PaginatedAdapter


class RouteListAdapter(

    private val mContext: AppCompatActivity
) : PaginatedAdapter<Sheets, RouteListAdapter.RouteListItem>()  {


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
        holder.itemRouteList.btnAction.setOnClickListener{
            listners.handleContinueButtonClick(data)
        }
        if(data.pick == false && data.drop ==false && data.visibility == false)
        {

        }
        else

        {

        }


    }



    inner class RouteListItem(val itemRouteList: ItemRouteListBinding) :
        RecyclerView.ViewHolder(itemRouteList.root)




}