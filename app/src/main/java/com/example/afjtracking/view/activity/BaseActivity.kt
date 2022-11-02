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
import com.example.afjtracking.utils.*
import com.example.afjtracking.websocket.SignalingClient
import com.example.afjtracking.websocket.listners.SignalingClientListener
import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.MessageType
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import java.util.*


open class BaseActivity : AppCompatActivity() {
    lateinit var isNetWorkConnected: Flow<Boolean>
    private lateinit var signallingClient: SignalingClient
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


        signallingClient = SignalingClient(
            Constants.WEBSOCKET_URL + "11",
            createSignallingClientListener()
        )
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
        toast("Exception Faced !!!")
    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            AFJUtils.writeLogs("Connection Established")
        }

        override fun onNewMessageReceived(messageModel: MessageModel) {
            //AFJUtils.writeLogs("Got New Message= $messageModel")
            try {
                when (messageModel.type) {
                    MessageType.CallResponse.value -> {
                    }
                    MessageType.OfferReceived.value -> {
                       /* runOnUiThread{
                            CustomDialog().showIncomingCallDialog(
                                this@BaseActivity,
                                messageModel.callerName.toString(),
                                positiveListener = {

                                },
                                negativeListener = {

                                }
                            )
                        }*/

                    }
                    MessageType.CallReject.value -> {
                    }
                    MessageType.CallClosed.value -> {
                    }
                    MessageType.AnswerReceived.value -> {
                    }
                    MessageType.ICECandidate.value -> {
                    }
                    else -> {
                    }
                }
            } catch (e: Exception) {
                AFJUtils.writeLogs("WebSocket: Message Parsing issue $e")
            }
        }

        override fun onCallEnded() {
        }
    }
}