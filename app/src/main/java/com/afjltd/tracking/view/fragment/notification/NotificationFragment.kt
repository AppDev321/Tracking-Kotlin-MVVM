package com.afjltd.tracking.view.fragment.notification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afjltd.tracking.model.responses.Notifications
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.CustomDialog
import com.afjltd.tracking.utils.SwipeListener
import com.afjltd.tracking.utils.SwipeToDelete
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.adapter.NotificationAdapter
import com.afjltd.tracking.view.adapter.NotificationItemListner
import com.afjltd.tracking.view.fragment.notification.viewmodel.NotificationViewModel
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.FragmentNotificationBinding


class NotificationFragment : Fragment(), NotificationItemListner {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    lateinit var notificationViewModel: NotificationViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity

    var mAdapter: NotificationAdapter? = null
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
        notificationViewModel.notificationData.observe(viewLifecycleOwner) {
            binding.baseLayout.visibility = View.VISIBLE

            mAdapter = NotificationAdapter(mBaseActivity, it.toMutableList(), this)
            binding.recNotification.layoutManager = LinearLayoutManager(mBaseActivity)
            binding.recNotification.adapter = mAdapter
            binding.recNotification.layoutManager = LinearLayoutManager(mBaseActivity)
            ItemTouchHelper(SwipeToDelete(object : SwipeListener {
                override fun onSwiped(pos: Int) {
                    mAdapter?.removeAt(pos)
                    mBaseActivity.showSnackMessage("Notification deleted", binding.root)
                    if (mAdapter?.itemCount == 0) {
                        binding.txtErrorMsg.visibility = View.VISIBLE
                        binding.txtErrorMsg.text = resources.getString(R.string.no_data_found)
                        binding.baseLayout.visibility = View.GONE
                    }
                }

            })).attachToRecyclerView(binding.recNotification)
        }


        return root
    }

    override fun onItemClick(item: Notifications) {
        notificationViewModel.updateNotificationStatus(mBaseActivity, item.id!!)
        when (item.type.toString().uppercase()) {
            AFJUtils.NOTIFICATIONTYPE.TEXT.name ->
            {
                        CustomDialog().showSimpleAlertMsg(
                            context = mBaseActivity,
                            title = item.notificationData?.title,
                            message = item.notificationData?.body,
                           textPositive = "Close")
            }

            AFJUtils.NOTIFICATIONTYPE.IMAGE.name ->
            {
                CustomDialog().createCustomTextImageDialog(
                    mBaseActivity,
                    title = item.notificationData?.title,
                   // message = item.notificationData?.body,
                    message = "Bilal will send me url of image in detail",
                    textNegative = "Close"
                )
            }

        }

    }

    override fun onItemDelete(item: Notifications) {
        notificationViewModel.deleteNotification(mBaseActivity, item.id!!)
    }

}

