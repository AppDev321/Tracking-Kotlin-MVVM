package com.example.afjtracking.view.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.example.afjtracking.R
import com.example.afjtracking.model.responses.QRFireDatabase
import com.example.afjtracking.model.responses.TrackingSettingFirebase
import com.example.afjtracking.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*


open class BaseActivity : AppCompatActivity() {
    lateinit var  isNetWorkConnected : Flow<Boolean>
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
            finish()
            startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
        } else {

            if (showToast) {
                if (msg.lowercase().contains("unable to resolve host")) {
                    InternetDialog(this).internetStatus
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
        toast("Exception Faced !!!")
    }
}