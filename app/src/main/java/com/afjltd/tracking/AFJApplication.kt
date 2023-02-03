package com.afjltd.tracking

import android.provider.Settings
import android.support.multidex.MultiDexApplication
import com.afjltd.tracking.firebase.FirebaseConfig
import com.afjltd.tracking.utils.Constants
import kotlinx.coroutines.GlobalScope


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