package com.example.afjtracking.view.fragment.auth

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentAuthBinding
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.viewmodel.AuthViewModel
import com.example.afjtracking.view.fragment.auth.viewmodel.QRFireDatabase
import com.example.afjtracking.view.fragment.fuel.viewmodel.AttendanceViewModel
import com.example.afjtracking.view.fragment.fuel.viewmodel.QRImageCallback
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

    }


    private lateinit var authListeners: AuthListeners
    private lateinit var mBaseActivity: NavigationDrawerActivity

    protected val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle() = lifecycleRegistry

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private var _authViewModel: AuthViewModel? = null
    private val authViewModel get() = _authViewModel!!


    var from: Int = 0

    private var _attendanceVM: AttendanceViewModel? = null
    private val attendanceVM get() = _attendanceVM!!
    var timer: CountDownTimer? = null

    val qrType = "TRACKING_APP_LOGIN"
    private lateinit var dbReference: DatabaseReference

    var isInitView :Boolean = true


    fun addAuthListner(authListeners: AuthListeners) {
        this.authListeners = authListeners
    }


    private fun init(context: Context) {
        mBaseActivity = context as NavigationDrawerActivity

        dbReference = FirebaseDatabase.getInstance().getReference("qr_table")


        _authViewModel = ViewModelProvider(context).get(AuthViewModel::class.java)
        _attendanceVM = ViewModelProvider(context).get(AttendanceViewModel::class.java)

        _binding =  FragmentAuthBinding.inflate((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater))

        binding.authmodel = authViewModel

        binding.containerLoginView.visibility = View.GONE
        binding.containerQrScan.visibility = View.VISIBLE


        // User data change listener
        dbReference.child(AFJUtils.getDeviceDetail().deviceID.toString())
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(QRFireDatabase::class.java)
                        if (user == null) {
                            showAuthOptionDialog()
                            return

                        }
                        if (AFJUtils.dateComparison(user.expiresAt.toString(), true)) {
                            if (user.status == false)
                            {
                                if(!isInitView)
                                mBaseActivity.toast("You are unable to access this form", true)
                                authListeners.onAuthCompletionListener(false)
                                showAuthOptionDialog()
                            }
                            else
                            {
                                //Save User object
                                AFJUtils.setUserToken(context,  user.data!!.token)
                                AFJUtils.saveObjectPref(
                                    context,
                                    AFJUtils.KEY_USER_DETAIL,
                                    user.data.user
                                )
                                authListeners.onAuthCompletionListener(true)
                            }
                        }
                        else
                        {
                                 if(!isInitView)
                                mBaseActivity.toast("QR code is expire please try again!!", true)
                                authListeners.onAuthCompletionListener(false)
                                showAuthOptionDialog()
                        }
                        isInitView = false
                    }
                    override fun onCancelled(error: DatabaseError) {
                        AFJUtils.writeLogs("FireError:$error")

                    }
                })

        addView(binding.root)
    }

    fun loginEmailPassword() {
        binding.containerQrScan.visibility = View.GONE
        binding.containerLoginView.visibility = View.VISIBLE
        authViewModel.user.observe(this) { loginUser ->
            if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).strEmailAddress)) {
                binding.txtEmailAddress.error = resources.getString(R.string.email_not_empty)
                binding.txtEmailAddress.requestFocus()
            } else if (!loginUser!!.isEmailValid) {
                binding.txtEmailAddress.error =resources.getString(R.string.email_not_valid)
                binding.txtEmailAddress.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).strPassword)) {
                binding.txtPassword.error = resources.getString(R.string.password_not_empty)
                binding.txtPassword.requestFocus()
            } else if (!loginUser.isPasswordLengthGreaterThan5) {
                binding.txtPassword.error =resources.getString(R.string.enter_valid_password)
                binding.txtPassword.requestFocus()
            } else {
                mBaseActivity.showProgressDialog(true)
                //    fuelViewModel.loginApiRequest(loginUser, this@LoginActivity)

            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }


    fun attendanceViewModel() {

        binding.containerLoginView.visibility = View.GONE
        binding.containerQrScan.visibility = View.VISIBLE

        attendanceVM.getQRCode(mBaseActivity,qrType)
        attendanceVM.showDialog.observe(this) {
            mBaseActivity.showProgressDialog(it)
        }

        attendanceVM.errorsMsg.observe(this, {
            if (it != null) {
                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)

                attendanceVM.errorsMsg.value = null
            }
        })
        try {
            attendanceVM.attendanceReponse.observe(this, {
                if (it != null) {

                    val response  = it
                    lifecycleScope.async(onPre = {
                        binding.layoutScan.txtTimeExpire.text = "Please wait QR Code is generating"
                        binding.layoutScan.idIVQrcode.setImageBitmap(null)
                    }, background = {
                        attendanceVM.getQrCodeBitmap(
                            it.qrCode,
                            mBaseActivity,
                        )
                    }, onPost = {
                        if(it != null)
                            binding.layoutScan.idIVQrcode.setImageBitmap(it)
                        timer =   object : CountDownTimer(1000 * response.timeOut.toLong(), 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                binding.layoutScan.txtTimeExpire.text =
                                    "Your QR Code will refresh in ${millisUntilFinished / 1000} seconds"
                            }
                            override fun onFinish() {
                                attendanceVM.getQRCode(mBaseActivity)
                                timer = null
                            }
                        }

                        if (timer != null)
                            timer?.start()
                    })



                    attendanceVM._attendanceResponse.value = null


                }

            })
        } catch (e: Exception) {
            AFJUtils.writeLogs(e.toString())
        }


    }

    fun showAuthOptionDialog() {


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
        })
     //   alert.show()



        attendanceViewModel()

    }

    private fun <R> CoroutineScope.async(onPre:() -> Unit, background: () -> R, onPost: (R) -> Unit) = launch(
        Dispatchers.Main) {
        onPre()
        withContext(Dispatchers.IO){
            background() }.let(onPost)
    }
}


