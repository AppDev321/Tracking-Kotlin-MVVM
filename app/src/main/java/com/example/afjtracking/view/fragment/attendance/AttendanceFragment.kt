package com.example.afjtracking.view.fragment.attendance

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.afjtracking.databinding.FragmentAttandenceScanBinding
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.viewmodel.AuthViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttandenceScanBinding? = null
    private val binding get() = _binding!!

    private var _attendanceVM: AuthViewModel? = null
    private val attendanceVM get() = _attendanceVM!!
    private var qrCodeType = ""
    companion object
    {
        const val ARG_ACTION_TYPE = "action_argument"
    }

    private lateinit var mBaseActivity: NavigationDrawerActivity
    var timer: CountDownTimer? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _attendanceVM = ViewModelProvider(this)[AuthViewModel::class.java]

        _binding = FragmentAttandenceScanBinding.inflate(inflater, container, false)
        val root: View = binding.root


        qrCodeType= arguments?.getString(ARG_ACTION_TYPE)!!



        attendanceVM.getQRCode(mBaseActivity,qrCodeType)
        attendanceVM.showDialog.observe(viewLifecycleOwner) {
         //   mBaseActivity.showProgressDialog(it)
        }

        attendanceVM.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)

                attendanceVM.errorsMsg.value = null
            }
        })


        try {
            attendanceVM.attendanceReponse.observe(viewLifecycleOwner, Observer {

                val response  = it
                if (response != null) {

                    lifecycleScope.async(onPre = {
                        binding.txtTimeExpire.text = "Please wait QR Code is generating"
                        binding.idIVQrcode.setImageBitmap(null)
                    }, background = {
                        attendanceVM.getQrCodeBitmap(
                            response.qrCode,
                            mBaseActivity,
                            )
                    }, onPost = {it->
                        if(it != null)
                        binding.idIVQrcode.setImageBitmap(it)
                        timer =   object : CountDownTimer(1000 * response.timeOut.toLong(), 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                binding.txtTimeExpire.text =
                                    "Your QR Code will refresh in ${millisUntilFinished / 1000} seconds"
                            }
                            override fun onFinish() {
                                attendanceVM.getQRCode(mBaseActivity,qrCodeType)
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
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (timer != null) {
            timer!!.cancel()

        }
    }

    private fun <R> CoroutineScope.async(onPre:() -> Unit, background: () -> R, onPost: (R) -> Unit) = launch(
        Dispatchers.Main) {
        onPre()
        withContext(Dispatchers.IO){
            background() }.let(onPost)
    }

}