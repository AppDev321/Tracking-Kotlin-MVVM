package com.example.afjtracking.view.fragment.fuel

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentNotificationBinding
import com.example.afjtracking.model.responses.Notifications
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.SwipeHelper
import com.example.afjtracking.utils.SwipeToDeleteCallback
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.NotificationAdapter
import com.example.afjtracking.view.adapter.NotificationItemListner
import com.example.afjtracking.view.fragment.fuel.viewmodel.NotificationViewModel


class NotificationFragment : Fragment(), NotificationItemListner {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    lateinit var notificationViewModel: NotificationViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        val root = binding.root

        mBaseActivity.toolbarVisibility(true)
        notificationViewModel.showDialog.observe(viewLifecycleOwner) {
           mBaseActivity.showProgressDialog(it)
        }

        notificationViewModel.getNotifications(mBaseActivity)
        notificationViewModel.errorsMsg.observe(viewLifecycleOwner) {
            if (it != null) {
                mBaseActivity.toast(it, false)
                binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
                binding.baseLayout.visibility = View.GONE
            }
        }


        notificationViewModel.getNotifications(mBaseActivity)
        notificationViewModel.notificationData.observe(viewLifecycleOwner){
            binding.baseLayout.visibility = View.VISIBLE

            val mAdapter = NotificationAdapter(mBaseActivity, it.toMutableList(),this)
            binding.recNotification.layoutManager =
                LinearLayoutManager(mBaseActivity, LinearLayoutManager.VERTICAL, false)
            binding.recNotification.adapter = mAdapter


           /* val swipeHandler = object : SwipeToDeleteCallback(mBaseActivity) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    mAdapter.removeAt(position)
                    mAdapter.notifyItemRemoved(position)
                    mAdapter.notifyItemRangeChanged(position,  mAdapter.getItemCount())


                    //notificationViewModel.deleteNotification(mBaseActivity,it[position].id!!)
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(   binding.recNotification)*/

      /*      object : SwipeHelper(mBaseActivity, binding.recNotification, false) {

                override fun instantiateUnderlayButton(
                    viewHolder: RecyclerView.ViewHolder?,
                    underlayButtons: MutableList<UnderlayButton>?
                ) {
                    // Archive Button
                    underlayButtons?.add(SwipeHelper.UnderlayButton(
                        "Delete",
                        AppCompatResources.getDrawable(
                         mBaseActivity,
                            R.drawable.ic_delete_white
                        ),
                        Color.parseColor("#FF0000"), Color.parseColor("#ffffff"),
                        UnderlayButtonClickListener { pos: Int ->
                            mAdapter.removeAt(pos)
                            mAdapter.notifyItemRemoved(pos)
                        }
                    ))



                }
            }*/

            binding.recNotification.layoutManager = LinearLayoutManager(mBaseActivity)

            val itemTouchHelper = ItemTouchHelper(object : SwipeHelper(binding.recNotification) {
                override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
                    var buttons = listOf<UnderlayButton>()
                    val deleteButton = deleteButton(position)
                  val markAsUnreadButton = markAsUnreadButton(position)
                   // val archiveButton = archiveButton(position)
                    when (position) {
                        //0 -> buttons = listOf(deleteButton)
                        0  -> buttons = listOf(deleteButton, markAsUnreadButton)
                       // 3 -> buttons = listOf(deleteButton, markAsUnreadButton, archiveButton)
                        else -> Unit
                    }
                    return buttons
                }
            })

            itemTouchHelper.attachToRecyclerView(binding.recNotification)

        }


        return root
    }
    private fun deleteButton(position: Int) : SwipeHelper.UnderlayButton {
        return SwipeHelper.UnderlayButton(
            mBaseActivity,
            "Delete",
            14.0f,
            android.R.color.holo_red_light,
            object : SwipeHelper.UnderlayButtonClickListener {
                override fun onClick() {
                    AFJUtils.writeLogs("Deleted item $position")
                }
            })
    }


    private fun markAsUnreadButton(position: Int) : SwipeHelper.UnderlayButton {
        return SwipeHelper.UnderlayButton(
            mBaseActivity,
            "Mark as unread",
            14.0f,
            android.R.color.holo_green_light,
            object : SwipeHelper.UnderlayButtonClickListener {
                override fun onClick() {
                    AFJUtils.writeLogs("Marked as unread item $position")
                }
            })
    }

    override fun onItemClick(item: Notifications) {
        notificationViewModel.updateNotificationStatus(mBaseActivity,item.id!!)
    }



}

