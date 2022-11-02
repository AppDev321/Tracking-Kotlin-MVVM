package com.example.afjtracking.view.fragment.auth.viewmodel


import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.R
import com.example.afjtracking.model.requests.FCMRegistrationRequest
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import retrofit2.Response


class AuthViewModel : ViewModel() {


    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow

    var _attendanceResponse = MutableLiveData<AttendanceReponse>()
    var attendanceReponse: LiveData<AttendanceReponse> = _attendanceResponse

   //  lateinit var attendanceReponseFlow: Flow<AttendanceReponse>

    private val _attendanceReponseFlow = MutableStateFlow<AttendanceReponse>(AttendanceReponse())
    val attendanceReponseFlow = _attendanceReponseFlow.asStateFlow()


    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: AuthViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): AuthViewModel? {
            if (instance == null) instance = AuthViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getQRCode(context: Context?, qrType: String = "ATTENDANCE") {
        val request = FCMRegistrationRequest()
        request.vehicleDeviceId = AFJUtils.getDeviceDetail().deviceID
        request.qrType = qrType
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getQRCode(request)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }
                override fun onSuccess(response: Response<LocationResponse?>) {
                    super.onSuccess(response)
                    val res = AttendanceReponse(
                        response.body()!!.data!!.attendanceCode!!,
                        response.body()!!.data!!.expireCodeSecond!!
                    )
                    _attendanceResponse.postValue(res)

                    _attendanceReponseFlow.value = res

                }
                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}

                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)

                }
                override fun onAPIError(error: String) {

                    mErrorsMsg!!.postValue(error)

                }
            })

    }


    fun getQrCodeBitmap(text: String, context: NavigationDrawerActivity, callback: QRImageCallback?) {

                try {
                    val size = 512 //pixels
                    val qrCodeContent = text
                    val hints =
                        hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }
                    // Make the QR code buffer border narrower
                    val bits =
                        QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)

                    var qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
                        for (x in 0 until size) {
                            for (y in 0 until size) {
                                it.setPixel(
                                    x,
                                    y,
                                    if (bits[x, y]) context.resources.getColor(R.color.black) else context.resources.getColor(
                                        R.color.all_app_bg
                                    )
                                )
                            }
                        }
                    }
                    //  var myLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo )
                    //  myLogo= getResizedBitmap(myLogo,80)
                    //  return mergeBitmaps(qrBitmap, myLogo)


                    callback?.onRendered(qrBitmap)


                } catch (e: Exception) {
                    callback?.onError(e)
                }



    }
    fun getQrCodeBitmap(text: String, context: AppCompatActivity):Bitmap? {

            try {
                val size = 512 //pixels
                val qrCodeContent = text
                val hints =
                    hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }
                // Make the QR code buffer border narrower
                val bits =
                    QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)

                var qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
                    for (x in 0 until size) {
                        for (y in 0 until size) {
                            it.setPixel(
                                x,
                                y,
                                if (bits[x, y]) context.resources.getColor(R.color.black) else context.resources.getColor(
                                    R.color.all_app_bg
                                )
                            )
                        }
                    }
                }
               /*  var myLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.afj_logo )
                 myLogo= Bitmap.createScaledBitmap(myLogo, 100, 100, false)
                  return mergeBitmaps(qrBitmap, myLogo)
*/

         return  qrBitmap


            } catch (e: Exception) {
               return   null
            }


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

data class AttendanceReponse(val qrCode: String ="", val timeOut: Int=0)
interface QRImageCallback {
    fun onRendered(bitmap: Bitmap)

    fun onError(e: Exception)
}
