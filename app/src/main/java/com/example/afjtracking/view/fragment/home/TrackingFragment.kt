package com.example.afjtracking.view.fragment.home

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.example.afjtracking.BuildConfig
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentTrakingBinding
import com.example.afjtracking.model.requests.FCMRegistrationRequest
import com.example.afjtracking.model.requests.LocationApiRequest
import com.example.afjtracking.model.responses.VehicleDetail
import com.example.afjtracking.service.location.LocationUpdatesService
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.home.viewmodel.TrackingViewModel
import com.google.android.material.snackbar.Snackbar


class TrackingFragment : Fragment() {

    private var _binding: FragmentTrakingBinding? = null
    private val binding get() = _binding!!


    private var _trackingViewModel: TrackingViewModel? = null
    private val trackingViewModel get() = _trackingViewModel!!

    private val TAG = TrackingFragment::class.java.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34


    private val LOCATION_PERMISSION_CODE = 1
    private val BACKGROUND_LOCATION_PERMISSION_CODE = 2
    private var myReceiver: TrackingFragment.MyReceiver? = null
    private var mService: LocationUpdatesService? = null
    private var mBound = false
    private var mLastClickTime: Long = 0
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mBound = true
            //Set Service to be active
            setButtonsState(AFJUtils.requestingLocationUpdates(mBaseActivity))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }




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

        myReceiver = MyReceiver()


        _trackingViewModel = ViewModelProvider(this).get(TrackingViewModel::class.java)
        _binding = FragmentTrakingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.trackingViewModel = trackingViewModel


        // Check that the user hasn't revoked permissions by going to Settings.
        if (AFJUtils.requestingLocationUpdates(activity)) {
            if (!checkPermission()) {
                requestPermissions()
            }
        }
        onSetViews()

        mBaseActivity.addChildFragment(MapsFragment(), this, R.id.frame_map)
        mBaseActivity.addChildFragment(VehicleDetailFragment(), this, R.id.frame_tracking)
        /*binding.btnInspection.setOnClickListener{
            mBaseActivity.moveFragmentCloseCurrent(
                binding.root,
                R.id.nav_vdi_inspection_create,
                R.id.tracking
            )
        }*/


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun onSetViews() {




        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        /*    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

           startForegroundService(new Intent(MainActivity.this, LocationUpdatesService.class));
        }
        else
        {*/

        mBaseActivity.bindService(
            Intent(mBaseActivity, LocationUpdatesService::class.java), mServiceConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )




        //}
        binding.btnTracking.setOnClickListener {
            var checkTrackingStatus = AFJUtils.requestingLocationUpdates(mBaseActivity)
            if (checkTrackingStatus == false) {
                if (!checkPermission()) {
                    requestPermissions()
                } else {
                    mService!!.requestLocationUpdates()
                    checkTrackingStatus = true
                }
            } else {
                mService!!.removeLocationUpdates()
                checkTrackingStatus = false

            }
            AFJUtils.setRequestingLocationUpdates(mBaseActivity, checkTrackingStatus)
            setButtonsState(AFJUtils.requestingLocationUpdates(mBaseActivity))

        }

        //Send token to server
        trackingViewModel.postFCMTokenToServer(FCMRegistrationRequest(fcmToken = Constants.DEVICE_FCM_TOKEN), context)

    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                mBaseActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return if (ContextCompat.checkSelfPermission(
                        mBaseActivity,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    true
                } else {
                    askPermissionForBackgroundUsage()
                    false
                }
            }
        } else {
            askForLocationPermission()
            return false
        }
        return true
    }

    private fun askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mBaseActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(mBaseActivity)
                .setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        mBaseActivity, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), LOCATION_PERMISSION_CODE
                    )
                }
                .setNegativeButton("CANCEL") { dialog, which ->
                    // Permission is denied by the user
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                mBaseActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mBaseActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(mBaseActivity)
                .setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        mBaseActivity, arrayOf(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ), BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                }
                .setNegativeButton("CANCEL") { dialog, which ->
                    // User declined for Background Location Permission.
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                mBaseActivity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(
                            mBaseActivity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        mService!!.requestLocationUpdates()
                    } else {
                        askPermissionForBackgroundUsage()
                    }
                }
            } else {
                permissionNotGranted()
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mService!!.requestLocationUpdates()
            } else {
                permissionNotGranted()
            }
        }
    }

    private fun permissionNotGranted() {
        setButtonsState(false)
        Snackbar.make(
            binding.root,
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID, null
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .show()
    }



    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(mBaseActivity).registerReceiver(
            myReceiver!!,
            IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
    }

    override fun onStop() {
        if (mBound) {

            mBaseActivity.unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            mBaseActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (shouldProvideRationale) {
            Snackbar.make(
                binding.root,
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) { // Request permission
                    ActivityCompat.requestPermissions(
                        mBaseActivity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
             ActivityCompat.requestPermissions(
                mBaseActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }


    private fun setButtonsState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            binding.btnTracking.text = "Stop Tracking"
            binding.btnTracking.backgroundTintList =
                AppCompatResources.getColorStateList(mBaseActivity, R.color.black)
            if(mService != null) {
                    if (!checkPermission()) {
                        requestPermissions()
                    } else {
                        mService!!.requestLocationUpdates()
                    }
            }



        } else {
            binding.btnTracking.text = "Start Tracking"
            binding.btnTracking.backgroundTintList =
                AppCompatResources.getColorStateList(mBaseActivity, R.color.colorPrimary)
        }


    }

     inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location =
                intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {
                val vehicleDetail =
                    AFJUtils.getObjectPref(
                        mBaseActivity,
                        AFJUtils.KEY_VEHICLE_DETAIL,
                        VehicleDetail::class.java )
                val request = LocationApiRequest()
                request.vehicleID = ""+ vehicleDetail.id
                request.accuracy = "" + location.accuracy
                request.altitude = "" + location.altitude
                request.heading = "" + location.bearing
                request.latitude = "" + location.latitude
                request.longitude = "" + location.longitude
                request.speed = "" + location.speed
                request.time = "" + location.time
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    request.speedAccuracy = "" + location.speedAccuracyMetersPerSecond
                } else {
                    request.speedAccuracy = "" + location.speed
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    request.isMocked = location.isMock
                } else {
                    request.isMocked = false
                }


                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return
                }
                else {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    if (trackingViewModel != null) {
                        AFJUtils.writeLogs("Location API: null ni hon")
                        trackingViewModel.postLocationData(request, context)
                    } else {
                        _trackingViewModel =
                            ViewModelProvider(context as ViewModelStoreOwner).get(TrackingViewModel::class.java)
                        AFJUtils.writeLogs("Location API: null hon ma")
                        trackingViewModel.postLocationData(request, context)
                    }
               }
            }
        }
    }

}