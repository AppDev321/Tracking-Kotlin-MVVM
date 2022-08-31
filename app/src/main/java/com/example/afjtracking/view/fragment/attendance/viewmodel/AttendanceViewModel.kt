package com.example.afjtracking.view.fragment.fuel.viewmodel


import android.content.Context
import android.graphics.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.R
import com.example.afjtracking.model.requests.FCMRegistrationRequest
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.utils.AFJUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AttendanceViewModel : ViewModel() {


    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow

     var _attendanceResponse = MutableLiveData<AttendanceReponse>()
    var attendanceReponse: LiveData<AttendanceReponse> = _attendanceResponse




    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: AttendanceViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): AttendanceViewModel? {
            if (instance == null) instance = AttendanceViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }



    fun getAttendanceData(context: Context?) {
        var request = FCMRegistrationRequest()
        request.vehicleDeviceId= AFJUtils.getDeviceDetail().deviceID
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getAttendanceQRCode(request)
       .enqueue(object : Callback<LocationResponse?> {
           override fun onResponse(
                call: Call<LocationResponse?>,
                    response: Response<LocationResponse?>
                ) {
                    _dialogShow.postValue(false)
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {

                            val res = AttendanceReponse(response.body()!!.data!!.attendanceCode!!,
                                response.body()!!.data!!.expireCodeSecond!!)

                                 _attendanceResponse.postValue(res)

                        } else {

                            var errors = ""
                            for (i in response.body()!!.errors!!.indices) {
                                errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                            }
                            mErrorsMsg!!.postValue(errors)
                        }
                    } else {

                        mErrorsMsg!!.postValue(response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                    val exception = t.toString()
                     mErrorsMsg!!.postValue(exception)




                }
            })

    }


    fun getQrCodeBitmap(text: String,context: Context): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = text
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }
        // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)

       var qrBitmap= Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
      //  var myLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo )
      //  myLogo= getResizedBitmap(myLogo,80)
      //  return mergeBitmaps(qrBitmap, myLogo)
        return qrBitmap
    }




    fun mergeBitmaps(qrCode: Bitmap, myLogo: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(qrCode.width, qrCode.height, qrCode.config)
        val canvas = Canvas(bmOverlay)

        canvas.drawBitmap(qrCode, Matrix(), null)
        canvas.drawBitmap(
            myLogo,
            ((qrCode.width - myLogo.width) / 2).toFloat(),
            ((qrCode.height - myLogo.height) / 2).toFloat(),
            null
        )
        return bmOverlay
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

}

data class AttendanceReponse(val qrCode:String ,val timeOut:Int)