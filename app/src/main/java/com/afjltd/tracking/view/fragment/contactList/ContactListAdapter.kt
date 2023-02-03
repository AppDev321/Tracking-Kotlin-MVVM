package com.afjltd.tracking.view.fragment.contactList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.model.responses.User
import com.afjltd.tracking.view.fragment.contactList.ContactListAdapter.ContactListViewHolder
import com.bumptech.glide.Glide
import com.afjltd.tracking.R

class ContactListAdapter(
    private val usersList: ArrayList<User>,
    private val context: Context,
    private val listener: ContactListClickListener
    ) :  RecyclerView.Adapter<ContactListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ContactListViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_contact_call, parent, false)
        return ContactListViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ContactListViewHolder, i: Int) {
        val data = usersList[i]
        viewHolder.mContactName.text = data.fullName
        Glide.with(context)
            .load(data.picture)
            .placeholder(R.drawable.ic_round_account_circle_)
            .error(R.drawable.ic_round_account_circle_)
            .into(viewHolder.mUserImage)
        viewHolder.mCallButton.setOnClickListener {

            listener.onVideoCallClick(data)
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

     class ContactListViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var mContactName: TextView
        var mUserImage: ImageView
        var mCallButton: ImageView

        init {
            mContactName = v.findViewById<View>(R.id.txt_contact_name) as TextView
            mUserImage = v.findViewById<View>(R.id.img_user) as ImageView
            mCallButton = v.findViewById<View>(R.id.btnVideoCall) as ImageView
        }
    }

    interface ContactListClickListener {
        fun onVideoCallClick(user :User)
    }
}