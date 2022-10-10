package com.example.afjtracking.view.activity

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.afjtracking.R
import com.example.afjtracking.databinding.ActivityNavigationBinding
import com.example.afjtracking.model.responses.QRFirebaseUser
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.InspectionSensor
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.nav_header_main.view.*


class NavigationDrawerActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var drawerLayout: DrawerLayout
    var dbReference: DatabaseReference? = null
    var timer: CountDownTimer? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.appBarMain.toolbar)

        toolbarVisibility(false)

        drawerLayout = binding.drawerLayout


        val navView: NavigationView = binding.navView


        try {
            val userObject =
                AFJUtils.getObjectPref(this, AFJUtils.KEY_USER_DETAIL, QRFirebaseUser::class.java)
            if (userObject.full_name != null) {
                navView.getHeaderView(0)
                    .txt_nav_head_designation.text =
                    userObject.official_email ?: Constants.NULL_DEFAULT_VALUE

                navView.getHeaderView(0)
                    .txt_nav_head_name.text = userObject.full_name
            }

        } catch (e: Exception) {
            writeExceptionLogs("Header Exception:\n${e}")
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)


        appBarConfiguration = AppBarConfiguration(
            setOf(
               /* R.id.tracking,
                R.id.nav_vdi_inspection_list,
                R.id.nav_weekly_inspection,
                R.id.nav_attendance_form,
                R.id.nav_fuel_form,
                R.id.nav_report_form,
                R.id.nav_device_info*/
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)


        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener({ menuItem ->
            AFJUtils.setUserToken(this, "")
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
            true
        })




    }


    fun toolbarVisibility(isShow: Boolean) {
        if (supportActionBar != null) {
            if (isShow) {
                supportActionBar?.show()
            }
            else {
                supportActionBar?.hide()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT)
        } else {
            super.onBackPressed()
        }
    }

    fun updateUserNavItem() {

        val navView: NavigationView = binding.navView
        try {
            val userObject =
                AFJUtils.getObjectPref(this, AFJUtils.KEY_USER_DETAIL, QRFirebaseUser::class.java)
            if (userObject != null) {
                navView.getHeaderView(0)
                    .txt_nav_head_designation.text =
                    userObject.official_email ?: Constants.NULL_DEFAULT_VALUE

                navView.getHeaderView(0)
                    .txt_nav_head_name.text = userObject.full_name ?: Constants.NULL_DEFAULT_VALUE
            }

        } catch (e: Exception) {
            writeExceptionLogs("Header Exception:\n${e}")
        }
    }
}