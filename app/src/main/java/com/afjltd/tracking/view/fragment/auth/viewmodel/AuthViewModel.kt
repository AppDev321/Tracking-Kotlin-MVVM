package com.afjltd.tracking.view.fragment.auth.viewmodel


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afjltd.tracking.model.requests.FCMRegistrationRequest
import com.afjltd.tracking.model.responses.LocationResponse
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.R
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.LoginResponse
import com.afjltd.tracking.view.activity.viewmodel.LoginViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response


class AuthViewModel : ViewModel() {


    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow

    var _attendanceResponse = MutableSharedFlow<AttendanceReponse>()
    var attendanceReponse = _attendanceResponse.asSharedFlow()


    private var mUserToken: MutableLiveData<String>? = MutableLiveData()


    private var mErrorsMsg = MutableSharedFlow<String>()
    val errorsMsg = mErrorsMsg.asSharedFlow()


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

    fun loginApiRequest(request: LoginRequest?, context: Context?,response : (Any)->Unit) {
        getInstance(context)
        apiInterface!!.getLoginUser(request).enqueue(object : SuccessCallback<LoginResponse?>() {
            override fun onSuccess(
                response: Response<LoginResponse?>
            ) {
                super.onSuccess(response)

                AFJUtils.setUserToken(context, response.body()!!.data!!.token)

                //Save User object
                AFJUtils.saveObjectPref(
                    context!!,
                    AFJUtils.KEY_USER_DETAIL,
                    response.body()!!.data!!.user
                )
                response(true)
            }
            override fun onFailure(response: Response<LoginResponse?>) {
                super.onFailure(response)
                var errors = ""
                for (i in response.body()!!.errors.indices) {
                    errors = """
                                $errors${response.body()!!.errors[i].message}

                                """.trimIndent()
                }

                viewModelScope.launch {
                    mErrorsMsg.emit(errors)
                }

                response(errors)
            }
            override fun onAPIError(error: String) {

                _dialogShow.postValue(false)
                viewModelScope.launch {
                    mErrorsMsg.emit(error)
                }
                response(error)
            }
        })
    }
    fun getQRCode(
        context: Context?, qrType: String = "ATTENDANCE",
        codeFetched: ((AttendanceReponse) -> Unit)? =null) {
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

                    viewModelScope.launch {
                        _attendanceResponse.emit(res)
                    }
                    if(codeFetched != null) {
                        codeFetched(res)
                    }
                }
                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}

                                """.trimIndent()
                    }

                    viewModelScope.launch {
                        mErrorsMsg.emit(errors)
                    }
                }
                override fun onAPIError(error: String) {

                    viewModelScope.launch {
                        mErrorsMsg.emit(error)
                    }
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
