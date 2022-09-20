package com.example.afjtracking.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.model.responses.Notifications

class NotificationAdapter
    (
    private val context: Context,
     var notificationList: MutableList<Notifications>,
    private val listner: NotificationItemListner

    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {

        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)

    }

    fun removeAt(position: Int) {
        notificationList.removeAt(position)

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationData = notificationList[position].notificationData
        holder.txtTitleNotification.text = notificationData?.title
        holder.txtDescNotification.text = notificationData?.body
        if (notificationList[position].isRead == 1)
        {
            holder.icNotification.setImageResource(R.drawable.ic_notification_list)
        }
        else
        {
            holder.icNotification.setImageResource(R.drawable.ic_notification_list_new)
        }

        holder.itemView.setOnClickListener{
            listner.onItemClick(notificationList[position])
        }
    }


    override fun getItemCount(): Int {
        return notificationList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val icNotification = itemView.findViewById<ImageView>(R.id.imgNotification)
        val txtTitleNotification = itemView.findViewById<TextView>(R.id.txtTitleNotificaiton)
        val txtDescNotification = itemView.findViewById<TextView>(R.id.txtDescNotificaiton)


    }

}

interface NotificationItemListner
{
    fun onItemClick(item:Notifications)
}
