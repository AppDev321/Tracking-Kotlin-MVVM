package com.example.afjtracking

import android.provider.Settings
import android.support.multidex.MultiDexApplication
import androidx.core.view.isGone
import com.example.afjtracking.firebase.FirebaseConfig
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.CustomDialog
import com.example.afjtracking.websocket.SignalingClient
import com.example.afjtracking.websocket.listners.SignalingClientListener
import com.example.afjtracking.websocket.model.IceCandidateModel
import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.MessageType
import com.google.gson.Gson

import kotlinx.coroutines.GlobalScope
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


class AFJApplication : MultiDexApplication() {

    val applicationScope = GlobalScope
    override fun onCreate() {
        super.onCreate()

        Constants.DEVICE_ID =  Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        FirebaseConfig.init()
        FirebaseConfig.setTokenFirebase(this)







    /*    CrashHandler.instance?.init(
            this,
            BuildConfig.DEBUG,
            true,
            0,
            LoginActivity::class.java)*/

    }




}