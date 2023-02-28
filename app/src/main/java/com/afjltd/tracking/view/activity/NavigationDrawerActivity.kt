package com.afjltd.tracking.view.activity

import android.app.Dialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.afjltd.tracking.callscreen.CallIncomingBroadcastReceiver
import com.afjltd.tracking.callscreen.CallkitIncomingPlugin
import com.afjltd.tracking.callscreen.EventListener
import com.afjltd.tracking.model.responses.QRFirebaseUser
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import com.afjltd.tracking.websocket.SignalingClient
import com.afjltd.tracking.websocket.VideoCallActivity
import com.afjltd.tracking.websocket.listners.SocketMessageListener
import com.afjltd.tracking.websocket.model.MessageModel
import com.afjltd.tracking.websocket.model.MessageType
import com.afjltd.tracking.R
import com.afjltd.tracking.broadcast.SocketBroadcast
import com.afjltd.tracking.databinding.ActivityNavigationBinding
import com.afjltd.tracking.model.responses.LoginResponse
import com.afjltd.tracking.utils.InternetDialog
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class NavigationDrawerActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var drawerLayout: DrawerLayout
    var dbReference: DatabaseReference? = null
    var timer: CountDownTimer? = null
    private val socketBroadCast = SocketBroadcast()
    lateinit var signallingClient: SignalingClient
    var internetDialog: Dialog? = null
    override fun onResume() {
        super.onResume()
        registerReceiver(
            socketBroadCast,
            IntentFilter(
                SocketBroadcast.SocketBroadcast.SOCKET_BROADCAST
            )
        )



    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(socketBroadCast)
    }

    override fun loginRequired() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.nav_login_form)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSocketConnection()


        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setSupportActionBar(binding.appBarMain.toolbar)
        }
        catch (e :java.lang.Exception)
        {
                AFJUtils.writeLogs("actionbar exception")
        }
        toolbarVisibility(false)
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
       /*
        try {
            val userObject =
                AFJUtils.getObjectPref(this, AFJUtils.KEY_USER_DETAIL, QRFirebaseUser::class.java)
            if (userObject.full_name != null) {
                navView.getHeaderView(0)
                    .txt_nav_head_designation.text =
                    userObject.official_email ?: Constants.NULL_DEFAULT_VALUE
                navView.getHeaderView(0)
                    .txt_nav_head_name.text = userObject.full_name ?: Constants.NULL_DEFAULT_VALUE
            }

        } catch (e: Exception) {
            writeExceptionLogs("Header Exception:\n${e}")
        }*/

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

        lifecycleScope.launch {
            isNetWorkConnected.collectLatest {
                if (!it) {
                    binding.appBarMain.contentMain.txtNetworkDesc.visibility = View.VISIBLE
                    internetDialog = InternetDialog(this@NavigationDrawerActivity).showNoInternetDialog()

                } else {
                    binding.appBarMain.contentMain.txtNetworkDesc.visibility = View.GONE
                    if (internetDialog != null) {
                        if (internetDialog?.isShowing == true) {
                            internetDialog?.dismiss()
                        }
                    }

                }
            }
        }
    }

    fun pressBackButton() {
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
             onBackPressedDispatcher.addCallback(this) {
                 if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT)
                 }
             }
         else */onBackPressed()
    }

    fun toolbarVisibility(isShow: Boolean) {
        if (supportActionBar != null) {
            if (isShow) {
                supportActionBar?.show()
            } else {
                supportActionBar?.hide()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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


    private fun createSignallingClientListener(socketURL: String) =
        object : SocketMessageListener() {
            override fun onConnectionEstablished() {
                AFJUtils.writeLogs("Connection Established")
            }

            override fun onNewMessageReceived(messageModel: MessageModel) {

                if (messageModel.type.equals(MessageType.IncomingCall.value)) {

                    /* Intent().also { intent ->
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


                    val currentUserId = messageModel.sendTo
                    val targetUserID = messageModel.sendFrom
                    CallkitIncomingPlugin.getInstance().showIncomingNotification(
                        messageModel.callerName.toString(),
                        false,
                        this@NavigationDrawerActivity
                    )
                    CallkitIncomingPlugin.setEventCallListener(object : EventListener() {
                        override fun send(event: String, body: Map<String, Any>) {
                            AFJUtils.writeLogs("call event = $event")
                            if (event == CallIncomingBroadcastReceiver.ACTION_CALL_ACCEPT) {

                                val intent = Intent(
                                    this@NavigationDrawerActivity,
                                    VideoCallActivity::class.java
                                ).apply {
                                    putExtra(VideoCallActivity.currentUserID, "" + currentUserId)
                                    putExtra(VideoCallActivity.targetUserID, "" + targetUserID)
                                    putExtra(VideoCallActivity.messageIntentValue, messageModel)
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

                                }
                                startActivity(intent)
                            } else if (event == CallIncomingBroadcastReceiver.ACTION_CALL_DECLINE) {
                                val answerCall = MessageModel(
                                    MessageType.CallEnd.value,
                                    currentUserId,
                                    targetUserID,
                                    0
                                )
                                signallingClient.sendMessageToWebSocket(answerCall)
                            }
                        }
                    })
                } else if (messageModel.type.equals(MessageType.CallAlreadyAnswered.value)) {
                    CallkitIncomingPlugin.getInstance().onMethodCall("endCall")
                }
            }

            override fun onConnectionClosed() {
                AFJUtils.writeLogs("Socket closed function")
                val userObject = AFJUtils.getObjectPref(
                    this@NavigationDrawerActivity,
                    AFJUtils.KEY_USER_DETAIL,
                    QRFirebaseUser::class.java
                )
                /*    if(userObject.id != null) {
                        signallingClient = SignalingClient.
                        getInstance(listener = this,
                            serverUrl =
                            socketURL)
                    } else {
                        signallingClient = SignalingClient.getInstance(listener =this,
                            serverUrl = socketURL)
                    }*/

            }

            override fun onWebSocketFailure(errorMessage: String) {

                AFJUtils.writeLogs("Socket Failure Dashboard => $errorMessage")
               // showSnackMessage("Socket Connection Issue: $errorMessage", binding.root)


            }

        }


    private fun requestSocketConnection() {

        val userObject = AFJUtils.getObjectPref(
            this@NavigationDrawerActivity,
            AFJUtils.KEY_USER_DETAIL,
            QRFirebaseUser::class.java
        )

        val currentUserId = if (userObject != null) {
            userObject.id  ?:1
        } else {
            val loginResponse = AFJUtils.getObjectPref(
                this,
                AFJUtils.KEY_LOGIN_RESPONSE,
                LoginResponse::class.java
            )
            loginResponse.data?.sosUser?.id!!
        }

        val socketURL =
            Constants.WEBSOCKET_URL + currentUserId + "&device=${Constants.WEBSOCKET_APP_NAME}"
        signallingClient = SignalingClient.getInstance(
            listener = createSignallingClientListener(socketURL),
            serverUrl = socketURL
        )
    }
}