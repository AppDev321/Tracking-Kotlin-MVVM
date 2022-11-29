package com.example.afjtracking.view.activity

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.Gravity
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.afjtracking.R
import com.example.afjtracking.broadcast.SocketBroadcast
import com.example.afjtracking.databinding.ActivityNavigationBinding
import com.example.afjtracking.model.responses.QRFirebaseUser
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.InternetDialog
import com.example.afjtracking.websocket.SignalingClient
import com.example.afjtracking.websocket.listners.SocketMessageListener
import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.MessageType
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sign


class NavigationDrawerActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var drawerLayout: DrawerLayout
    var dbReference: DatabaseReference? = null
    var timer: CountDownTimer? = null
    private val socketBroadCast = SocketBroadcast()
    lateinit var signallingClient: SignalingClient


    override fun onResume() {
        super.onResume()
        registerReceiver(
            socketBroadCast,
            IntentFilter(
               SocketBroadcast.SocketBroadcast.SOCKET_BROADCAST
            )
        )

        if(::signallingClient.isInitialized)
        {
            //Connection restart on every resume
            Handler().postDelayed({
                signallingClient.destroy()
            }, 1000)

        }

        val userObject = AFJUtils.getObjectPref(this, AFJUtils.KEY_USER_DETAIL, QRFirebaseUser::class.java)
        if(userObject.id != null) {
            signallingClient = SignalingClient.
            getInstance(listener = createSignallingClientListener(),
                serverUrl = Constants.WEBSOCKET_URL +  userObject.id +"&device=Tracking")
        } else {
            signallingClient = SignalingClient.getInstance(listener = createSignallingClientListener(),
                serverUrl = Constants.WEBSOCKET_URL + "1" +"&device=Tracking")
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(socketBroadCast)
    }




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




        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            AFJUtils.setUserToken(this, "")
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
            true
        }


        lifecycleScope.launch{
            isNetWorkConnected.collectLatest {
                if(!it) {
                    binding.appBarMain.contentMain.txtNetworkDesc.visibility = View.VISIBLE
                    InternetDialog(this@NavigationDrawerActivity).showNoInternetDialog()
                }
                else {
                    binding.appBarMain.contentMain.txtNetworkDesc.visibility = View.GONE
                }
            }
        }

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


    private fun createSignallingClientListener() = object : SocketMessageListener() {
        override fun onConnectionEstablished() {
            AFJUtils.writeLogs("Connection Established")
        }
        override fun onNewMessageReceived(messageModel: MessageModel) {

            if(messageModel.type.equals( MessageType.OfferReceived.value)) {
                AFJUtils.writeLogs("Got New Message Navigation= $messageModel")
                /*Intent().also { intent ->
                    intent.action = SocketBroadcast.SocketBroadcast.SOCKET_BROADCAST
                    intent.putExtra(
                        SocketBroadcast.SocketBroadcast.intentData,
                        SocketBroadcast.SocketBroadcast.SOCKET_MESSAGE_RECEIVED
                    )
                    val bundle = Bundle()
                    bundle.putSerializable(  SocketBroadcast.SocketBroadcast.intentValues, messageModel  )
                    intent.putExtras(bundle)
                    sendBroadcast(intent)
                }*/

                val intent = Intent(this@NavigationDrawerActivity, IncomingCallScreen::class.java).apply {
                    putExtra(IncomingCallScreen.intentData, messageModel)
                    flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

                }
                startActivity(intent)
            }
        }

        override fun onConnectionClosed() {
            AFJUtils.writeLogs("Socket closed function")
            val userObject = AFJUtils.getObjectPref(this@NavigationDrawerActivity, AFJUtils.KEY_USER_DETAIL, QRFirebaseUser::class.java)
            if(userObject.id != null) {
                signallingClient = SignalingClient.
                getInstance(listener = this,
                    serverUrl =
                    Constants.WEBSOCKET_URL +  userObject.id +  "&device=Tracking")
            } else {
                signallingClient = SignalingClient.getInstance(listener =this,
                    serverUrl = Constants.WEBSOCKET_URL + "1" +"&device=Tracking")
            }

        }

        override fun onWebSocketFailure(errorMessage: String) {
           AFJUtils.writeLogs("Socket Failure => $errorMessage")
        }

    }
}