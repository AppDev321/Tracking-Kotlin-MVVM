package com.example.afjtracking.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil

import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.BuildConfig
import com.example.afjtracking.R
import com.example.afjtracking.databinding.ActivitySplashBinding
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.responses.VehicleDetail
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.viewmodel.LoginViewModel
import java.util.*


class SplashActivity : BaseActivity() {
    lateinit var loginViewModel: LoginViewModel
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // val token = AFJUtils.getUserToken(this@SplashActivity)
        //if (! token!!.isEmpty()) {
      /*  val vehicleDetail = AFJUtils.getObjectPref(
            this,
            AFJUtils.KEY_VEHICLE_DETAIL,
            VehicleDetail::class.java
        )
        if (vehicleDetail != null) {
            finish()
            startActivity(Intent(this@SplashActivity, NavigationDrawerActivity::class.java))
        }*/


        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding = DataBindingUtil.setContentView(this@SplashActivity, R.layout.activity_splash)
        binding.lifecycleOwner = this
        binding.loginViewModel = loginViewModel


        var deviceData = AFJUtils.getDeviceDetail()
        if (deviceData != null) {
            val loginUser = LoginRequest(deviceDetail = deviceData)
            loginViewModel.loginApiRequest(loginUser, this@SplashActivity)
        }

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

}