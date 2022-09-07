package com.example.afjtracking.view.fragment.attendance

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.databinding.FragmentAttandenceScanBinding
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.fuel.viewmodel.AttendanceViewModel
import com.example.afjtracking.view.fragment.fuel.viewmodel.QRImageCallback


class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttandenceScanBinding? = null
    private val binding get() = _binding!!

    private var _attendanceVM: AttendanceViewModel? = null
    private val attendanceVM get() = _attendanceVM!!

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
        _attendanceVM = ViewModelProvider(this).get(AttendanceViewModel::class.java)

        _binding = FragmentAttandenceScanBinding.inflate(inflater, container, false)
        val root: View = binding.root


        attendanceVM.getQRCode(mBaseActivity)
        attendanceVM.showDialog.observe(viewLifecycleOwner) {
            mBaseActivity.showProgressDialog(it)
        }

        attendanceVM.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)

                attendanceVM.errorsMsg.value = null
            }
        })

        binding.txtTimeExpire.text = "Please wait QR Code is generating"


        try {
            attendanceVM.attendanceReponse.observe(viewLifecycleOwner, Observer {
                if (it != null) {

                    attendanceVM.getQrCodeBitmap(
                        it.qrCode,
                        mBaseActivity,
                        object : QRImageCallback {
                            override fun onRendered(bitmap: Bitmap) {

                                mBaseActivity.runOnUiThread {
                                    binding.idIVQrcode.setImageBitmap(bitmap)
                                    timer =
                                        object : CountDownTimer(1000 * it.timeOut.toLong(), 1000) {
                                            override fun onTick(millisUntilFinished: Long) {
                                                binding.txtTimeExpire.text =
                                                    "Your QR Code will refresh in ${millisUntilFinished / 1000} seconds"
                                            }

                                            override fun onFinish() {
                                                attendanceVM.getQRCode(mBaseActivity)
                                                timer = null
                                            }
                                        }

                                    if (timer != null)
                                        timer?.start()
                                }
                            }


                            override fun onError(e: Exception) {
                                AFJUtils.writeLogs("There is some exception in rendering QR code")
                            }

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


}