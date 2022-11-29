package com.example.afjtracking.view.fragment.auth


import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.*
import com.example.afjtracking.databinding.DialogChooseSigninBinding
import com.example.afjtracking.databinding.FragmentAuthBinding
import com.example.afjtracking.model.responses.QRFireDatabase
import com.example.afjtracking.model.responses.QRFirebaseUser
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.InternetDialog
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.viewmodel.AuthViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*

class CustomAuthenticationView : FrameLayout, LifecycleOwner {
    constructor(context: Context, attributes: AttributeSet, style: Int) : super(
        context,
        attributes,
        style
    ) {
        init(context)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }


    interface AuthListeners {
        fun onAuthCompletionListener(boolean: Boolean)
        fun onAuthForceClose(boolean: Boolean)

    }


    private lateinit var authListeners: AuthListeners
    private lateinit var mBaseActivity: NavigationDrawerActivity


    protected val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle() = lifecycleRegistry

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!


    var from: Int = 0

    private var _authViewModel: AuthViewModel? = null
    private val authViewModel get() = _authViewModel!!


    val qrType = "TRACKING_APP_LOGIN"

    var isInitView: Boolean = true
    var isRequestInitiated = false

    fun addAuthListener(authListeners: AuthListeners) {
        this.authListeners = authListeners
    }

    private fun cancelCountDownTimer() {
        if (mBaseActivity.timer != null) {
            mBaseActivity.timer?.cancel()
            mBaseActivity.timer = null
            mBaseActivity.timer = mBaseActivity.timer


        }
    }

    private fun init(context: Context) {
        mBaseActivity = context as NavigationDrawerActivity


        _authViewModel = ViewModelProvider(context)[AuthViewModel::class.java]

        _binding =
            FragmentAuthBinding.inflate((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater))

        binding.authmodel = authViewModel

        binding.containerLoginView.visibility = View.GONE
        binding.containerQrScan.visibility = View.VISIBLE

        // User data change listener
        InternetDialog(context).internetStatus

        mBaseActivity.dbReference =
            FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_QR_TABLE)
        mBaseActivity.dbReference?.child(AFJUtils.getDeviceDetail().deviceID.toString())
            ?.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var isAuthSuccess = false
                        try {
                            val user = dataSnapshot.getValue(QRFireDatabase::class.java)

                            if (user == null) {
                                cancelCountDownTimer()
                                showAuthOptionDialog()
                                return
                            }
                            if (user.has_error == true) {
                                if (!isInitView) {
                                    mBaseActivity.showSnackMessage(
                                        user.error_message.toString(),
                                        binding.root
                                    )
                                }
                            } else {

                                AFJUtils.setUserToken(context, user.data!!.token)
                                AFJUtils.saveObjectPref(
                                    context,
                                    AFJUtils.KEY_USER_DETAIL,
                                    user.data.user
                                )
                                if(!isInitView) {
                                    isAuthSuccess = true
                                }
                            }
                          //  cancelCountDownTimer()
                            if (isAuthSuccess) {
                                cancelCountDownTimer()
                                authListeners.onAuthCompletionListener(isAuthSuccess)
                                context.updateUserNavItem()
                            } else {
                               if(isInitView) {
                                    showAuthOptionDialog()
                              }
                            }

                            if(isInitView)
                            {
                               cancelCountDownTimer()
                            }
                            isInitView = false


                        }
                        catch (e: Exception) {
                            mBaseActivity.showSnackMessage(
                                "There is some issue in parsing data of authentication",
                                binding.root
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {


                    }
                })

        /*  }
          else{
                 // mBaseActivity.dbReference!!.child(AFJUtils.getDeviceDetail().deviceID.toString())
                 showAuthOptionDialog()
             }*/



        addView(binding.root)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }


    private fun attendanceViewModel() {
        isRequestInitiated = true
        if (mBaseActivity.timer == null) {
            AFJUtils.writeLogs("Getting request QR code")
            binding.layoutScan.idIVQrcode.setImageBitmap(null)
            binding.layoutScan.txtTimeExpire.text = ""
            binding.containerLoginView.visibility = View.GONE
            binding.containerQrScan.visibility = View.VISIBLE
            authViewModel.getQRCode(mBaseActivity, qrType)
        }


        authViewModel.errorsMsg.observe(this) { it ->
            if (it != null) {
                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)
                authViewModel.errorsMsg.value = null
            }

        }


            authViewModel.attendanceReponse.observe(this, Observer {
                    if (it != null) {
                        if (mBaseActivity.timer == null) {

                            if(isRequestInitiated) {

                                fetchAndGenerateQRCode(it.qrCode, it.timeOut)
                                isRequestInitiated = false
                            }

                        }
                        authViewModel._attendanceResponse.value = null

                    }

            })

    }

    private fun fetchAndGenerateQRCode(qrCode: String, timeout: Int) {
     lifecycleScope. async(onPre = {
                binding.layoutScan.txtTimeExpire.text =
                    "Please wait QR Code is generating"
                binding.layoutScan.idIVQrcode.setImageBitmap(null)
            }, background = {
                AFJUtils.writeLogs("QR code bakcground")
                authViewModel.getQrCodeBitmap(
                    qrCode,
                    mBaseActivity,
                )
            }, onPost = {

                AFJUtils.writeLogs("QR code received")
                if (it != null) {
                    cancelCountDownTimer()

                    binding.layoutScan.idIVQrcode.setImageBitmap(it)
                    mBaseActivity.timer =
                        object :
                            CountDownTimer(1000 * timeout.toLong(), 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                binding.layoutScan.txtTimeExpire.text =
                                    "Your QR Code will refresh in ${millisUntilFinished / 1000} seconds"
                            }

                            override fun onFinish() {

                                cancelCountDownTimer()
                                //  authViewModel.getQRCode(mBaseActivity, qrType)
                                showAuthOptionDialog()


                            }
                        }

                    if (mBaseActivity.timer != null)
                        mBaseActivity.timer?.start()
                }
            })





    }


    fun showAuthOptionDialog() {


/*
        binding.containerLoginView.visibility = View.GONE
        binding.containerQrScan.visibility = View.VISIBLE

        binding.layoutScan.idIVQrcode.setImageBitmap(null)
        binding.layoutScan.txtTimeExpire.text =""

        val choice = arrayOf<CharSequence>("QR Scan", "Password")
        val alert: AlertDialog.Builder = AlertDialog.Builder(mBaseActivity)
        alert.setTitle("Choose Authentication Option")
        alert.setSingleChoiceItems(choice, 0,
            DialogInterface.OnClickListener { dialog, which ->
                if (choice[which] === "QR Scan") {
                    from = 0
                } else if (choice[which] === "Password") {
                    from = 1
                }
            })
        alert.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            if (from == 0) {

                attendanceViewModel()
            } else {

                loginEmailPassword()
            }
        })*/
        //   alert.show()
        val userObject =
            AFJUtils.getObjectPref(context, AFJUtils.KEY_USER_DETAIL, QRFirebaseUser::class.java)
        if (userObject.full_name != null) {
            try {
                showCredentialSessionDialog(userObject)
            } catch (e: Exception) {
                mBaseActivity.showSnackMessage(
                    "There is some issue in user already login form",
                    binding.root
                )
            }
        } else {
            binding.containerLoginView.visibility = View.GONE
            binding.containerQrScan.visibility = View.VISIBLE
            attendanceViewModel()
        }
    }


    private fun showCredentialSessionDialog(userObject: QRFirebaseUser) {
        val builder = android.app.AlertDialog.Builder(context)
        val dialogBinding: DialogChooseSigninBinding =
            DialogChooseSigninBinding.inflate(LayoutInflater.from(context), null, false)
        val mView: View = dialogBinding.root
        builder.setCancelable(false)
        builder.setView(mView)
        dialogBinding.user = userObject

        val alertDialog = builder.create()
        alertDialog.show()
        dialogBinding.btnClose.setOnClickListener {
            alertDialog.dismiss()
            authListeners.onAuthForceClose(true)
        }
        dialogBinding.containerAlreadySignin.setOnClickListener {
            alertDialog.dismiss()
            authListeners.onAuthCompletionListener(true)
        }
        dialogBinding.containerAnotherSignin.setOnClickListener {
            alertDialog.dismiss()
            attendanceViewModel()
        }

    }

    fun closeAllPendingOperations() {

    }

    companion object {
        private var INSTANCE: CustomAuthenticationView? = null

        fun killPrevInstance() {
            if (INSTANCE != null) {
                INSTANCE!!.closeAllPendingOperations()
                INSTANCE = null
            }
        }

        fun instance(
            context: Context,
            authListeners: CustomAuthenticationView.AuthListeners
        ): CustomAuthenticationView? {
            if (INSTANCE != null) {
                //                return INSTANCE
                killPrevInstance()
            }
            synchronized(this) {
                INSTANCE = CustomAuthenticationView(context)
                INSTANCE!!.addAuthListener(authListeners)
                return INSTANCE
            }
        }

    }

    private fun <R> CoroutineScope.async(
        onPre: () -> Unit,
        background: () -> R,
        onPost: (R) -> Unit
    ) = launch(
        Dispatchers.Main
    ) {
        onPre()
        withContext(Dispatchers.IO) {
            background()
        }.let(
            onPost
        )
    }


}

