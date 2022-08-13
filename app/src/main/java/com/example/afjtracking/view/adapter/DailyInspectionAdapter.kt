package com.example.afjtracking.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.databinding.LayoutWeeklyInspectionItemBinding
import com.example.afjtracking.model.responses.Inspections
import com.example.afjtracking.utils.AFJUtils


class DailyInspectionAdapter(

    private val mContext: AppCompatActivity,
    private var inspectionList: List<Inspections>
) : RecyclerView.Adapter<DailyInspectionAdapter.WeeklyInspectionItem>() {


    lateinit var listners: ClickWeeklyInspectionListner


    fun setListnerClick(listener: ClickWeeklyInspectionListner) {
        this.listners = listener
    }


    override fun getItemCount(): Int {
        return inspectionList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyInspectionItem {

        val itemView = DataBindingUtil.inflate(
            LayoutInflater.from(mContext), R.layout.layout_weekly_inspection_item,
            parent, false
        ) as LayoutWeeklyInspectionItemBinding
        return WeeklyInspectionItem(itemView)
    }


    override fun onBindViewHolder(holder: WeeklyInspectionItem, position: Int) {

        val data = inspectionList[position]
        holder.itemWeeklyInspection.txtCreatedDate.text =
            AFJUtils.convertServerDateTime(data.createdAt.toString(), true)
        holder.itemWeeklyInspection.txtInspectionDate.text = data.date
        holder.itemWeeklyInspection.txtInspectionType.text = data.vehicleType
        holder.itemWeeklyInspection.txtInspectionStatus.text = data.status
        holder.itemWeeklyInspection.txtVehicleType.text = data.vehicleType
        holder.itemWeeklyInspection.btnContinueInspection.text = "View Inspection"
        holder.itemWeeklyInspection.txtErrorCount.text = data.count


        holder.itemWeeklyInspection.containerError.visibility = View.VISIBLE


        holder.itemWeeklyInspection.btnContinueInspection.setOnClickListener {

            if (!data.count.equals("0")) {
                listners.handleContinueButtonClick(data)
            }

        }

        if (data.count.equals("0")) {
            holder.itemWeeklyInspection.btnContinueInspection.text = "Completed"
            holder.itemWeeklyInspection.btnContinueInspection.backgroundTintList =
                AppCompatResources.getColorStateList(mContext, R.color.green)
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    inner class WeeklyInspectionItem(val itemWeeklyInspection: LayoutWeeklyInspectionItemBinding) :
        RecyclerView.ViewHolder(itemWeeklyInspection.root) {

    }

    interface ClickWeeklyInspectionListner {

        fun handleContinueButtonClick(data: Inspections)

    }
}