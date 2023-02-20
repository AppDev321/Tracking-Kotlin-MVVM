package com.afjltd.tracking.view.activity

import android.app.ActivityManager
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.afjltd.tracking.R
import com.afjltd.tracking.utils.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import java.util.*


open class BaseActivity : AppCompatActivity() {
    lateinit var isNetWorkConnected: Flow<Boolean>

    open fun loginRequired() {}


    override fun onStart() {
        super.onStart()
        AFJUtils.setPeriodicWorkRequest(this)


    }

/*    private fun UploadUtil.trs(){
        this.handler
    }

    private fun <R> CoroutineScope.async(onPre:() -> Unit, background: () -> R, onPost: (R) -> Unit) = launch(
        Dispatchers.Main) {
        onPre()
        withContext(Dispatchers.IO){
            background() }.let(onPost)
    }*/


    lateinit var progressDialog: ProgressDialog
    lateinit var customProgressDialog: LottieDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        isNetWorkConnected = LiveNetworkState(this).isConnected


        customProgressDialog = CustomDialog().initializeProgressDialog(
            this,
            lottieFile = R.raw.progress,


            )

        /* UploadUtil().withBackgroundExecutionListener(object: BackgroundExecutionListener{
             override fun onPre() {

             }

             override fun onPost() {

             }
         })
        */
        progressDialog = ProgressDialog(this@BaseActivity)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please Wait....")


    }


    fun showSnackMessage(msg: String, view: View) {
        Snackbar.make(
            view,
            msg,
            Snackbar.LENGTH_SHORT
        ).show()
    }


    fun showProgressDialog(isShow: Boolean) {
        if (isShow) {
            /* if (!progressDialog.isShowing)
                 progressDialog.show()*/
            if (!customProgressDialog.isShowing)
                customProgressDialog.show()
        } else {
            /* if (progressDialog.isShowing)
                 progressDialog.dismiss()*/
            if (customProgressDialog.isShowing)
                customProgressDialog.dismiss()
        }
    }

    fun toast(msg: String, showToast: Boolean = true) {

        AFJUtils.writeLogs("Api Error:$msg")

        if (msg.lowercase(Locale.getDefault())
                .contains(resources.getString(R.string.unauthenticated))
        ) {
            AFJUtils.setUserToken(this@BaseActivity, "")
            //   finish()
            // startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
            // loginRequired()

        } else {

            if (showToast) {
                if (msg.lowercase().contains("unable to resolve host")) {
                InternetDialog(this).showNoInternetDialog()
                } else {
                    Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    fun moveFragmentCloseCurrent(
        view: View,
        newFragmentID: Int,
        oldFragmentId: Int,
        argument: Bundle? = null
    ) {
        try {
            Navigation.findNavController(view)
                .navigate(
                    newFragmentID,
                    argument, NavOptions.Builder()
                        .setPopUpTo(oldFragmentId, true)
                        .build()
                )

        } catch (e: Exception) {
            writeExceptionLogs(e.toString())
        }
    }

    fun closeFragment(fragment: Fragment) {
        try {

            lifecycleScope.launchWhenResumed {
                NavHostFragment.findNavController(fragment).navigateUp()
            }
        } catch (e: Exception) {
            writeExceptionLogs(e.toString())
        }

    }

    fun moveFragmentToNextFragment(
        view: View,
        newFragmentID: Int,
        argument: Bundle? = null
    ) {
        try {
            Navigation.findNavController(view)
                .navigate(
                    newFragmentID,
                    argument
                )

        } catch (e: Exception) {
            writeExceptionLogs(e.toString())
        }
    }


    fun addChildFragment(fragment: Fragment, currentFragment: Fragment, frameId: Int) {

        try {
            val transaction = currentFragment.childFragmentManager.beginTransaction()
            transaction.add(frameId, fragment).commit()
        } catch (e: Exception) {
            writeExceptionLogs(e.toString())
        }


    }


    fun writeExceptionLogs(crash: String?) {
        AFJUtils.writeLogs("$crash")
        toast("Exception Faced !!! --> $crash")
    }

    protected fun turnOnScreen() {
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
             this.setShowWhenLocked(true)
             this.setTurnScreenOn(true)
             val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
             keyguardManager.requestDismissKeyguard(this, null)

         } else {
             window.addFlags(
                 WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                         or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                         or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                         or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
             )
         }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

    }

    protected fun fullscreen(view: View) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            WindowInsetsControllerCompat(window, view).let {
                it.hide(WindowInsetsCompat.Type.systemBars())
                it.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .moveTaskToFront(taskId, 0)
        } catch (e: Exception) {
            Log.w("", e.toString())
        }
    }
}