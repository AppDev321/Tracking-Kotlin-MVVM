package com.afjltd.tracking.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.ActivitySplashBinding
import com.afjltd.tracking.firebase.FirebaseConfig
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.CustomDialog
import com.afjltd.tracking.utils.ErrorCodes
import com.afjltd.tracking.view.activity.viewmodel.LoginViewModel
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode


class SplashActivity : BaseActivity() {
    lateinit var loginViewModel: LoginViewModel
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        FirebaseConfig.init()
        FirebaseConfig.setTokenFirebase(this)


        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = DataBindingUtil.setContentView(
            this@SplashActivity,
            R.layout.activity_splash
        )
        binding.lifecycleOwner = this
        binding.loginViewModel = loginViewModel
        try {
            supportActionBar?.hide()
        } catch (e: Exception) {
        }



        binding.pbCircular.visibility = View.VISIBLE
        binding.txtError.visibility = View.GONE
        binding.containerButton.visibility = View.GONE

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
                            try {
                                moveToNextScreen()
                            } catch (e: Exception) {
                                AFJUtils.writeLogs("Exception while moving to next screen")
                            }
                        } else {

                            val dialog1 = Dialog(this, R.style.df_dialog)
                            dialog1.setContentView(R.layout.dialog_no_internet)
                            dialog1.setCancelable(true)
                            dialog1.setCanceledOnTouchOutside(true)
                            dialog1.findViewById<View>(R.id.btnSpinAndWinRedeem)
                                .setOnClickListener { finish() }
                            dialog1.show()
                            /*  CustomDialog().showSimpleAlertMsg(this, "Error",
                                  "There is some issue while getting default values, please contact to admin",
                                  textNegative = "Close",
                                  negativeListener = {

                                  })*/
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


    @SuppressLint("SuspiciousIndentation")
    private fun moveToNextScreen() {

        val deviceData = AFJUtils.getDeviceDetail()
        val loginUser = LoginRequest()
        loginUser.deviceDetail= deviceData

        loginViewModel.loginApiRequest(loginUser, this@SplashActivity)
        binding.containerButton.visibility = View.GONE
        loginViewModel.userToken.observe(this) { s ->
            if (s != null) {
                finish()
                startActivity(Intent(this@SplashActivity, NavigationDrawerActivity::class.java))
            }
        }
        loginViewModel.errorsMsg.observe(this) { s ->
            if (s != null ) {

                binding.pbCircular.visibility = View.GONE
                binding.txtError.visibility = View.VISIBLE
                binding.containerButton.visibility = View.VISIBLE

                if(s.toString().lowercase().contains("null")) {
                    binding.txtError.text = ErrorCodes.errorMessage + ErrorCodes.deviceGettingError
                    binding.btnLogin.text = "Retry"
                }
                else
                {

                    binding.txtError.text = s.toString()
                    if (s.contains("not assigned")) {
                        binding.btnLogin.text = "QR Scan"
                    } else {
                        binding.btnLogin.text = "Retry"
                    }

                }
            }
        }

        binding.btnLogin.setOnClickListener {
            try {
                if (binding.btnLogin.text.toString().lowercase() == "retry") {
                    val deviceData = AFJUtils.getDeviceDetail()
                    val loginUser = LoginRequest(deviceDetail = deviceData)
                    loginViewModel.loginApiRequest(loginUser, this@SplashActivity)
                    binding.pbCircular.visibility = View.VISIBLE
                    binding.txtError.visibility = View.GONE
                    binding.containerButton.visibility = View.GONE
                } else {
                    scanQrCodeLauncher.launch(null)
                }
            } catch (e: Exception) {
                binding.txtError.text = ErrorCodes.errorMessage + ErrorCodes.splashLoginButton
                binding.btnLogin.text = "Retry"
            }
        }
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
                binding.txtError.text =   ErrorCodes.qrNotValid
            }
        } else {
            binding.txtError.text = ErrorCodes.qrScanningIssue
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