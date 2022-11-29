package com.example.afjtracking.view.activity

import android.Manifest
import android.Manifest.permission.READ_PHONE_STATE
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.ActivitySplashBinding
import com.example.afjtracking.firebase.FirebaseConfig
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.responses.VehicleDetail
import com.example.afjtracking.ota.ForceUpdateChecker
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.CustomDialog
import com.example.afjtracking.view.activity.viewmodel.LoginViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.permissionx.guolindev.PermissionX
import java.util.*


class SplashActivity : BaseActivity(), ForceUpdateChecker.OnUpdateNeededListener {
    lateinit var loginViewModel: LoginViewModel
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE

                ).request{ allGranted, _ ,_ ->
                if (allGranted){
                    FirebaseConfig.fetchLocationServiceTime()

                    loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
                    binding = DataBindingUtil.setContentView(this@SplashActivity, R.layout.activity_splash)
                    binding.lifecycleOwner = this
                    binding.loginViewModel = loginViewModel

                    ForceUpdateChecker.with(this).onUpdateNeeded(this).check()
                } else {
                    CustomDialog().showSimpleAlertMsg(this,"Alert",
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
            .setPositiveButton("Update Now",
                DialogInterface.OnClickListener { dialog, which -> redirectStore(updateUrl) })
            .setNegativeButton("Close",
                DialogInterface.OnClickListener { dialog, which -> finish() }).create()
        dialog.show()
    }

    override fun onAppUptoDate() {

        val deviceData = AFJUtils.getDeviceDetail()
        val loginUser = LoginRequest(deviceDetail = deviceData)
        //Support Contact List
        loginViewModel.getContactList(this@SplashActivity)

        loginViewModel.loginApiRequest(loginUser, this@SplashActivity)

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
                //showProgressDialog(false)
                // toast(s)
                binding.pbCircular.visibility = View.GONE
                binding.txtError.visibility = View.VISIBLE
                binding.txtError.text = s.toString()
                binding.containerButton.visibility = View.VISIBLE
            }
        }

    }
    private fun redirectStore(updateUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}