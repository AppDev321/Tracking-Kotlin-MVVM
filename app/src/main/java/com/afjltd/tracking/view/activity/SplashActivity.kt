package com.afjltd.tracking.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.ActivitySplashBinding
import com.afjltd.tracking.firebase.FirebaseConfig
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.ota.ForceUpdateChecker
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.CustomDialog
import com.afjltd.tracking.view.activity.viewmodel.LoginViewModel
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.content.QRContent


class SplashActivity : BaseActivity(),
    ForceUpdateChecker.OnUpdateNeededListener {
    lateinit var loginViewModel: LoginViewModel
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        FirebaseConfig.init()
        FirebaseConfig.setTokenFirebase(this)


        // val token = AFJUtils.getUserToken(this@SplashActivity)
        //if (! token!!.isEmpty()) {
        /*val vehicleDetail = AFJUtils.getObjectPref(
            this,
            AFJUtils.KEY_VEHICLE_DETAIL,
            VehicleDetail::class.java
        )
        if (vehicleDetail != null) {
            finish()
            startActivity(Intent(this@SplashActivity, NavigationDrawerActivity::class.java))
        }*/



        PermissionX.init(this)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                } else {
                    Manifest.permission.CAMERA
                }

            ).request { allGranted, _, _ ->
                if (allGranted) {
                    FirebaseConfig.fetchLocationServiceTime()
                    {
                        if (it) {
                            loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
                            binding = DataBindingUtil.setContentView(
                                this@SplashActivity,
                                R.layout.activity_splash
                            )
                            binding.lifecycleOwner = this
                            binding.loginViewModel = loginViewModel

                            ForceUpdateChecker.with(this).onUpdateNeeded(this).check()
                        } else {
                            CustomDialog().showSimpleAlertMsg(this, "Error",
                                "There is some issue while getting default values, please contact to admin",
                                textNegative = "Close",
                                negativeListener = {
                                    finish()
                                })
                        }
                    }


                } else {
                    CustomDialog().showSimpleAlertMsg(this, "Alert",
                        "Please allow permission for working",
                        textNegative = "Close",
                        negativeListener = {
                            finish()
                        })
                }
            }

    }


    override fun onUpdateNeeded(updateUrl: String) {
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("New Update Available")
            .setMessage("There is a newer version of app available please update it now.")
            .setPositiveButton("Update Now"
            ) { _, _ -> redirectStore(updateUrl) }
            .setNegativeButton("Close",
            ) { _, _ -> finish() }.create()
        dialog.show()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onAppUptoDate() {

        val deviceData = AFJUtils.getDeviceDetail()
        val loginUser = LoginRequest(deviceDetail = deviceData)
        //Support Contact List
        loginViewModel.getContactList(this@SplashActivity)

        loginViewModel.loginApiRequest(loginUser, this@SplashActivity)
        binding.containerButton.visibility = View.GONE

        loginViewModel.user.observe(this) { loginUser ->
            //    showProgressDialog(true)

            binding.pbCircular.visibility = View.VISIBLE
            binding.txtError.visibility = View.GONE
            binding.containerButton.visibility = View.GONE

            loginViewModel.loginApiRequest(loginUser, this@SplashActivity)


        }
        loginViewModel.userToken.observe(this) { s ->
            if (s != null) {
                //  showProgressDialog(false)
                //  AFJUtils.setUserToken(this@SplashActivity, s)
                finish()
                startActivity(Intent(this@SplashActivity, NavigationDrawerActivity::class.java))
            }
        }



        loginViewModel.errorsMsg.observe(this) { s ->
            if (s != null) {

                binding.pbCircular.visibility = View.GONE
                binding.txtError.visibility = View.VISIBLE
                binding.txtError.text = s.toString()
                binding.containerButton.visibility = View.VISIBLE


                   if (s.contains("not assigned")) {
                       binding.btnLogin.text = "QR Scan"
                   } else {
                       binding.btnLogin.text = "Retry"
                   }
            }
        }

        binding.btnLogin.setOnClickListener {
            if (binding.btnLogin.text.toString().lowercase() == "retry") {
                val deviceData = AFJUtils.getDeviceDetail()
                val loginUser = LoginRequest(deviceDetail = deviceData)
                loginViewModel.userMutableLiveData!!.postValue(loginUser)
            } else {
                scanQrCodeLauncher.launch(null)
            }
        }

    }

    private fun redirectStore(updateUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        vibratePhone()
        if (result is QRResult.QRSuccess) {
            val data = result.content.rawValue
            try {
                val qrResponseData = Gson().fromJson(data, QRActivateDeviceData::class.java)
                val loginUser = LoginRequest(
                    deviceDetail = AFJUtils.getDeviceDetail(),
                    employeID = qrResponseData.employee_id,
                    vehicleID = qrResponseData.vehicle_id
                )
                loginViewModel.loginApiRequest(loginUser, this@SplashActivity)
            } catch (e: Exception) {
                binding.txtError.text =
                    "QR Response is not same as per expected, Please contact admin"
            }
        } else {
            binding.txtError.text = "There is some issue in scanning QR Code, Please contact admin"
        }


    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {

                it.vibrate(100)
            }
        }
    }
}


data class QRActivateDeviceData(val vehicle_id: String, val employee_id: String)