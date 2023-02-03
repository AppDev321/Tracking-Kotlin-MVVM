package com.afjltd.tracking.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.model.responses.WeeklyInspectionData
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.PaginatedAdapter
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.LayoutWeeklyInspectionItemBinding


class WeeklyInspectionAdapter(

    private val mContext: AppCompatActivity
) : PaginatedAdapter<WeeklyInspectionData, WeeklyInspectionAdapter.WeeklyInspectionItem>()  {


    lateinit var listners: ClickListenerInterface



    fun setListnerClick(listener: ClickListenerInterface) {
        this.listners = listener
    }

    override fun createCustomViewHolder(parent: ViewGroup, viewType: Int): WeeklyInspectionItem {
        val itemView = DataBindingUtil.inflate(
            LayoutInflater.from(mContext), R.layout.layout_weekly_inspection_item,
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
                AppCompatResources.getColorStateList(mContext, R.color.green)
        }


    }



    inner class WeeklyInspectionItem(val itemWeeklyInspection: LayoutWeeklyInspectionItemBinding) :
        RecyclerView.ViewHolder(itemWeeklyInspection.root)






}