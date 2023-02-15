package com.afjltd.tracking.view.fragment.home

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.Context.ACTIVITY_SERVICE
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.broadcast.TrackingAppBroadcast
import com.afjltd.tracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.NOTIFICATION_BROADCAST

import com.afjltd.tracking.model.requests.FCMRegistrationRequest
import com.afjltd.tracking.model.requests.LocationApiRequest
import com.afjltd.tracking.model.responses.*
import com.afjltd.tracking.service.location.ForegroundLocationService
import com.afjltd.tracking.utils.*
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.fragment.contactList.ContactListAdapter
import com.afjltd.tracking.view.fragment.home.viewmodel.TrackingViewModel
import com.afjltd.tracking.websocket.VideoCallActivity
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.FragmentTrakingBinding
import com.permissionx.guolindev.PermissionX
import kotlin.math.log


class TrackingFragment : Fragment() {

    private var _binding: FragmentTrakingBinding? = null
    private val binding get() = _binding!!


    private var _trackingViewModel: TrackingViewModel? = null
    private val trackingViewModel get() = _trackingViewModel!!


    private var locationService: ForegroundLocationService? = null
    private var locationServiceBound = false


    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundLocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
            //Set Service to be active
            setButtonsState(AFJUtils.getRequestingLocationUpdates(mBaseActivity))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            locationServiceBound = false
        }
    }


    private lateinit var mBaseActivity: NavigationDrawerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity

    }

    private val notificationBroadCast = object : TrackingAppBroadcast() {
        override fun refreshNotificationCount() {
            super.refreshNotificationCount()
            trackingViewModel.getNotificationCount(mBaseActivity)
        }

        override fun trackingSetting() {
            super.trackingSetting()
            setButtonsState(AFJUtils.getRequestingLocationUpdates(mBaseActivity))


        }

        override fun onLocationReceived(location: Location?) {
            super.onLocationReceived(location)
            val vehicleDetail =
                AFJUtils.getObjectPref(
                    mBaseActivity,
                    AFJUtils.KEY_VEHICLE_DETAIL,
                    VehicleDetail::class.java
                )
            val request = LocationApiRequest()
            request.vehicleID = "" + vehicleDetail.id
            request.accuracy = "" + location?.accuracy
            request.altitude = "" + location?.altitude
            request.heading = "" + location?.bearing
            request.latitude = "" + location?.latitude
            request.longitude = "" + location?.longitude
            request.speed = "" + location?.speed
            request.time = "" + location?.time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                request.speedAccuracy = "" + location?.speedAccuracyMetersPerSecond
            } else {
                request.speedAccuracy = "" + location?.speed
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                request.isMocked = location?.isMock
            } else {
                request.isMocked = false
            }
            /*    val trackingViewModel =
                    ViewModelProvider(context as ViewModelStoreOwner)[TrackingViewModel::class.java]
            */

            trackingViewModel.postLocationData(request, context)

            //********Save this request as well for sending data to Route Sheet Status
            AFJUtils.saveObjectPref(mBaseActivity, AFJUtils.KEY_LOCATION_REQUEST_OBJECT, request)
///**************************************************************************************************
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        PermissionX.init(this)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,

                ).request { allGranted, _, _ ->
                if (allGranted) {
                    subscribeToLocationUpdates()
                } else {
                    Toast.makeText(
                        mBaseActivity,
                        "you should accept all permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }




        _trackingViewModel = ViewModelProvider(this)[TrackingViewModel::class.java]
        _binding = FragmentTrakingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.trackingViewModel = trackingViewModel
        mBaseActivity.toolbarVisibility(false)

        mBaseActivity.addChildFragment(MapsFragment(), this, R.id.frame_map)


        val vehicleLoginResponse =
            AFJUtils.getObjectPref(
                mBaseActivity,
                AFJUtils.KEY_LOGIN_RESPONSE,
                LoginResponse::class.java
            )
        /*  if(vehicleLoginResponse.data?.isSupportCallEnabled ==false)
          {
              binding.btnHelpLineCall.visibility = View.INVISIBLE
          }
          else
          {
              binding.btnHelpLineCall.visibility = View.VISIBLE
          }*/

        val menuFragment =
            MainMenuFragment.getInstance(menuItemsList = vehicleLoginResponse.data!!.vehicleMenu)
        mBaseActivity.addChildFragment(
            menuFragment,
            this,
            R.id.frame_tracking
        )

        binding.txtGreeting.text = AFJUtils.getGreetingMessage()


        //Save vehicle object
        val vehicleDetail = vehicleLoginResponse.data?.vehicle as VehicleDetail
        binding.txtVRN.text = vehicleDetail.vrn ?: Constants.NULL_DEFAULT_VALUE
        binding.txtOdoMeter.text = vehicleDetail.odometerReading ?: Constants.NULL_DEFAULT_VALUE
        binding.txtType.text = vehicleDetail.type ?: Constants.NULL_DEFAULT_VALUE
        binding.txtModel.text = vehicleDetail.model ?: Constants.NULL_DEFAULT_VALUE
        binding.txtTypeVehicle.text =
            vehicleDetail.detail?.vehicleType ?: Constants.NULL_DEFAULT_VALUE


        val userObject = AFJUtils.getObjectPref(
            mBaseActivity,
            AFJUtils.KEY_USER_DETAIL,
            QRFirebaseUser::class.java
        )
        binding.txtDriver.text = userObject.full_name ?: "N/A"


        //Send token to server
        trackingViewModel.postFCMTokenToServer(
            FCMRegistrationRequest(
                fcmToken = Constants.DEVICE_FCM_TOKEN,
                vehicle_id = vehicleDetail.id,
                vehicleDeviceId = Constants.DEVICE_ID
            ),
            context
        )


        binding.txtNotificationCount.visibility = View.GONE
        trackingViewModel.getNotificationCount(mBaseActivity)

        trackingViewModel.notificationCount.observe(viewLifecycleOwner) {
            binding.txtNotificationCount.visibility = View.VISIBLE
            binding.txtNotificationCount.text = it.toString()

        }

        binding.btnNotification.setOnClickListener {
            mBaseActivity.moveFragmentToNextFragment(
                binding.root,
                R.id.nav_notification
            )
        }


        binding.btnHelpLineCall.setOnClickListener {

            val listUser = AFJUtils.getObjectPref(
                mBaseActivity,
                AFJUtils.KEY_CONTACT_LIST_PREF,
                ContactListData::class.java
            )

            if (listUser == null) {
                mBaseActivity.showSnackMessage("No Support Contact found", root)
                return@setOnClickListener
            }

            if (listUser.contactUserList.size <= 0) {
                mBaseActivity.showSnackMessage("No Support Contact found", root)

            } else {

                val userObject = AFJUtils.getObjectPref(
                    mBaseActivity,
                    AFJUtils.KEY_USER_DETAIL,
                    QRFirebaseUser::class.java
                )
                val currentUserId =   if (userObject.id != null) {
                    userObject.id
                } else {
                    val loginResponse = AFJUtils.getObjectPref(
                        mBaseActivity,
                        AFJUtils.KEY_LOGIN_RESPONSE,
                        LoginResponse::class.java
                    )
                    loginResponse.data?.sosUser?.id!!

                }

                AFJUtils.writeLogs("caller user id = $currentUserId")

                val mDialogView = LayoutInflater.from(mBaseActivity)
                    .inflate(R.layout.custom_dialog_contact_list, null)
                val mBuilder = AlertDialog.Builder(mBaseActivity)
                    .setView(mDialogView)
                //show dialog
                val mAlertDialog = mBuilder.show()
                val btnClose = mDialogView.findViewById<ImageView>(R.id.btnCancel)
                val listContact = mDialogView.findViewById<RecyclerView>(R.id.list_contact)
                listContact.layoutManager = LinearLayoutManager(mBaseActivity)
                val adapter = ContactListAdapter(listUser.contactUserList, mBaseActivity,
                    object : ContactListAdapter.ContactListClickListener {
                        override fun onVideoCallClick(user: User) {

                            mAlertDialog.dismiss()

                            val currentUserId = currentUserId
                            val targetUserID = user.id.toString()
                            val intent =
                                Intent(mBaseActivity, VideoCallActivity::class.java).apply {
                                    putExtra(
                                        VideoCallActivity.currentUserID,
                                        "" + currentUserId
                                    )
                                    putExtra(VideoCallActivity.targetUserID, "" + targetUserID)
                                }
                            mBaseActivity.startActivity(intent)
                        }
                    })
                listContact.adapter = adapter


                btnClose.setOnClickListener {
                    mAlertDialog.dismiss()
                }


            }

        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(mBaseActivity, ForegroundLocationService::class.java)
        mBaseActivity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)



        mBaseActivity.registerReceiver(
            notificationBroadCast,
            IntentFilter(
                NOTIFICATION_BROADCAST
            )
        )

        if (binding != null) {
            val userObject = AFJUtils.getObjectPref(
                mBaseActivity,
                AFJUtils.KEY_USER_DETAIL,
                QRFirebaseUser::class.java
            )
            binding.txtDriver.text = userObject.full_name ?: "N/A"
        }
    }

/*
    override fun onResume() {
        super.onResume()



        mBaseActivity.registerReceiver(
            notificationBroadCast,
            IntentFilter(
                NOTIFICATION_BROADCAST
            )
        )

        if (binding != null) {
            val userObject = AFJUtils.getObjectPref(
                mBaseActivity,
                AFJUtils.KEY_USER_DETAIL,
                QRFirebaseUser::class.java
            )
            binding.txtDriver.text = userObject.full_name ?: "N/A"


    }
     }*/

    override fun onDestroy() {
        super.onDestroy()

        mBaseActivity.unregisterReceiver(notificationBroadCast)
        if (locationServiceBound) {

            mBaseActivity.unbindService(mServiceConnection)
            locationServiceBound = false
        }
    }


    private fun setButtonsState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            if (locationService != null) {

                subscribeToLocationUpdates()
            }
        } else {
            CustomDialog().showTaskCompleteDialog(
                mBaseActivity,
                isShowTitle = true,
                isShowMessage = true,
                titleText = "Critical Message",
                msgText = "Your vehicle tracking is off, Please contact to admin",
                lottieFile = R.raw.alert,
                showOKButton = true,
                okButttonText = "Close",
                listner = object : DialogCustomInterface {
                    override fun onClick(var1: LottieDialog) {
                        super.onClick(var1)
                        var1.dismiss()

                    }
                }
            )
            // unsubscribeToLocationUpdates()
        }


    }


    private fun subscribeToLocationUpdates() {

        val serviceStatus = mBaseActivity.isServiceRunning(ForegroundLocationService::class.java)
        if (!serviceStatus) {
            val serviceIntent = Intent(mBaseActivity, ForegroundLocationService::class.java)
            mBaseActivity.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }

        locationService?.subscribeToLocationUpdates()
    }

    private fun unsubscribeToLocationUpdates() {

        locationService?.unsubscribeToLocationUpdates()

    }

    private fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        return (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Integer.MAX_VALUE)
            .any { it -> it.service.className == service.name }
    }
}