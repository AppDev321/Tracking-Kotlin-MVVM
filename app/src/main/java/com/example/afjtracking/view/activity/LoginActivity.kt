package com.example.afjtracking.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.ActivityLoginBinding
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.viewmodel.LoginViewModel
import java.util.*


class LoginActivity : BaseActivity() {
    lateinit   var loginViewModel: LoginViewModel;
    lateinit var binding: ActivityLoginBinding
   val  READ_PHONE_STATE = 210
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionState()
        val token = AFJUtils.getUserToken(this@LoginActivity)
        if (! token!!.isEmpty()) {
            finish()
            startActivity(Intent(this@LoginActivity, NavigationDrawerActivity::class.java))
        }
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding = DataBindingUtil.setContentView(this@LoginActivity, R.layout.activity_login)
        binding.setLifecycleOwner(this)
        binding.loginViewModel = loginViewModel


        loginViewModel.user.observe(this) { loginUser ->
            if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).strEmailAddress)) {
                binding.txtEmailAddress.error = "Enter an E-Mail Address"
                binding.txtEmailAddress.requestFocus()
            } else if (!loginUser!!.isEmailValid) {
                binding.txtEmailAddress.error = "Enter a Valid E-mail Address"
                binding.txtEmailAddress.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).strPassword)) {
                binding.txtPassword.error = "Enter a Password"
                binding.txtPassword.requestFocus()
            } else if (!loginUser.isPasswordLengthGreaterThan5) {
                binding.txtPassword.error = "Enter at least 6 Digit password"
                binding.txtPassword.requestFocus()
            } else {
                AFJUtils.setUserToken(this@LoginActivity, "")
                showProgressDialog(true)
                loginViewModel.loginApiRequest(loginUser, this@LoginActivity)
            }
        }
        loginViewModel.userToken.observe(this) { s ->
            if (s != null) {
                showProgressDialog(false)
                AFJUtils.setUserToken(this@LoginActivity, s)
                finish()
                startActivity(Intent(this@LoginActivity, NavigationDrawerActivity::class.java))
            }
        }
        loginViewModel.errorsMsg.observe(this) { s ->
            if (s != null) {
                showProgressDialog(false)
                toast(s)
            }
        }
    }

fun checkPermissionState()
{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val permissions = arrayOf<String>(Manifest.permission.READ_PHONE_STATE)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(permissions, READ_PHONE_STATE)
        }
    } else {
        try {
            val telephonyManager =
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val imei = telephonyManager.deviceId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_PHONE_STATE -> if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) try {

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}