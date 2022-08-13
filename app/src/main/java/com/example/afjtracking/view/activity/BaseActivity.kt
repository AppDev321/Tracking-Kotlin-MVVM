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
import com.example.afjtracking.utils.AFJUtils
import com.google.android.material.snackbar.Snackbar


open class BaseActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
    AFJUtils.setPeriodicWorkRequest(this)

    }

/*
    private fun UploadUtil.trs(){
        this.handler
    }

    private fun <R> CoroutineScope.async(onPre:() -> Unit, background: () -> R, onPost: (R) -> Unit) = launch(
        Dispatchers.Main) {
        onPre()
        withContext(Dispatchers.IO){
            background() }.let(onPost)
    }
*/


    lateinit var progressDialog: ProgressDialog;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



       /* UploadUtil().withBackgroundExecutionListener(object: BackgroundExecutionListener{
            override fun onPre() {

            }

            override fun onPost() {

            }
        })
        lifecycleScope.async(onPre = {

        }, background = {

        }, onPost = {

        })*/
        progressDialog = ProgressDialog(this@BaseActivity)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please Wait....")

    }


    fun showSnackMessage(msg:String ,view: View)
    {
        Snackbar.make(
         view,
            msg,
            Snackbar.LENGTH_SHORT
        ).show()
    }


    fun showProgressDialog(isShow: Boolean) {
        if (isShow) {
            if (!progressDialog.isShowing)
                progressDialog.show()
        } else {
            if (progressDialog.isShowing)
                progressDialog.dismiss()
        }
    }

    fun toast(msg: String,showToast: Boolean = true) {
        if (msg.toLowerCase().contains("unauthenticated")) {
            AFJUtils.setUserToken(this@BaseActivity, "")
            finish()
            startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
        } else {
           if(showToast)
            Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun moveFragmentCloseCurrent(
        view: View,
        newFragmentID: Int,
        oldFragmentId: Int,
        argument: Bundle? = null
    ) {
        try{
        Navigation.findNavController(view)
            .navigate(
                newFragmentID,
                argument, NavOptions.Builder()
                    .setPopUpTo(oldFragmentId, true)
                    .build()
            )

        }
        catch (e: Exception)
        {
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
        try{
            Navigation.findNavController(view)
                .navigate(
                    newFragmentID,
                    argument
                )

        }
        catch (e: Exception)
        {
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