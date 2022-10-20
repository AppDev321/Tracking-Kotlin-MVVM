package com.example.afjtracking.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.databinding.LayoutWeeklyInspectionItemBinding
import com.example.afjtracking.model.responses.WeeklyInspectionData
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.PaginatedAdapter


class WeeklyInspectionAdapter(

    private val mContext: AppCompatActivity
) : PaginatedAdapter<WeeklyInspectionData, WeeklyInspectionAdapter.WeeklyInspectionItem>()  {


    lateinit var listners: ClickWeeklyInspectionListener



    fun setListnerClick(listener: ClickWeeklyInspectionListener) {
        this.listners = listener
    }

    override fun createCustomViewHolder(parent: ViewGroup, viewType: Int): WeeklyInspectionItem {
        val itemView = DataBindingUtil.inflate(
            LayoutInflater.from(mContext), com.example.afjtracking.R.layout.layout_weekly_inspection_item,
            parent, false
        ) as LayoutWeeklyInspectionItemBinding
        return WeeklyInspectionItem(itemView)
    }


    override fun customBindHolder(holder: WeeklyInspectionItem, position: Int) {

        val data = getItem(position)
        holder.itemWeeklyInspection.txtCreatedDate.text = AFJUtils.convertServerDateTime(data.createdAt.toString(),true)
        holder.itemWeeklyInspection.txtInspectionDate.text = data.date
        holder.itemWeeklyInspection.txtInspectionType.text = data.type
        holder.itemWeeklyInspection.txtInspectionStatus.text = data.status
        holder.itemWeeklyInspection.txtVehicleType.text = data.vehicleType



        if(data.status == mContext.resources.getString(R.string.initiated)) {

            holder.itemWeeklyInspection.btnContinueInspection.setOnClickListener {
                listners.handleContinueButtonClick(data)
            }
        }
        else
        {
            holder.itemWeeklyInspection.btnContinueInspection.text = "Completed"
            holder.itemWeeklyInspection.btnContinueInspection.backgroundTintList =
                AppCompatResources.getColorStateList(mContext, com.example.afjtracking.R.color.green)
        }


    }



    inner class WeeklyInspectionItem(val itemWeeklyInspection: LayoutWeeklyInspectionItemBinding) :
        RecyclerView.ViewHolder(itemWeeklyInspection.root)


    interface ClickWeeklyInspectionListener {
        fun<T> handleContinueButtonClick(data: T)
    }



}