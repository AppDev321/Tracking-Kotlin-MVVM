package com.afjltd.tracking.view.fragment.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afjltd.tracking.BuildConfig
import com.afjltd.tracking.model.requests.FCMRegistrationRequest
import com.afjltd.tracking.model.requests.LocationApiRequest
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.ApiVersionResponse
import com.afjltd.tracking.model.responses.LocationResponse
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.room.model.TableLocation
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import com.google.mlkit.common.sdkinternal.CommonUtils.getAppVersion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class TrackingViewModel : ViewModel() {

    private val _notificationCount = MutableSharedFlow<Int>()
    val notificationCount = _notificationCount.asSharedFlow()


    private var _locationRequest = MutableSharedFlow<LocationApiRequest>()

    val getLocationRequest = _locationRequest.asSharedFlow()

    private var mErrorsMsg = MutableSharedFlow<String>()

    val errorsMsg = mErrorsMsg.asSharedFlow()
    val APP_NAME = "TRACKING"

    companion object {
        private var instance: TrackingViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): TrackingViewModel? {
            if (instance == null) instance = TrackingViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun postLocationData(request: LocationApiRequest?, context: Context?) {
        getInstance(context)

        apiInterface!!.updateLocation(request)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {
                    super.onSuccess(response)
                    AFJUtils.writeLogs("Location API =${response.body()!!.data!!.message.toString()}")
                    viewModelScope.launch {
                        _locationRequest.emit(request!!)
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
                    super.onAPIError(error)
                    val exception = error

                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        var locationTable = TableLocation(
                            apiName = Constants.LOCATION_API,
                            apiPostData = AFJUtils.convertObjectToJson(request!!),
                            apiPostResponse = "",
                            apiError = "",
                            apiRetryCount = 0,
                            lastTimeApiError = "",
                            apiRequestTime = AFJUtils.getCurrentDateTime(),
                            apiResponseTime = "",
                            errorPosted = "0"

                        )
                        // LocationTableRepo.insertLocationData(context!!, locationTable)
                        AFJUtils.writeLogs("Location Data insert in table")


                    } else {

                        viewModelScope.launch {
                            mErrorsMsg.emit(exception)
                        }
                    }
                }
            })

    }


    fun checkApiVersion(context: Context, callbackListener: OnUpdateNeededListener) {
        getInstance(context)
        apiInterface!!.checkApiStatus().enqueue(object : SuccessCallback<ApiVersionResponse?>() {
            override fun onSuccess(
                response: Response<ApiVersionResponse?>
            ) {
                super.onSuccess(response)

                val appList = response.body()?.data?.appData ?: arrayListOf()

                for (app in appList) {
                    if (app.appName.toString().uppercase() == APP_NAME.uppercase()) {
                        AFJUtils.writeLogs("${BuildConfig.VERSION_NAME} ==${app.version.toString()} ")
                        if (app.version.toString() != BuildConfig.VERSION_NAME) {
                            if(app.downloadUrl.toString().isNotEmpty()) {
                                callbackListener.onUpdateNeeded(app.downloadUrl.toString())
                            }
                            break
                        }
                    }
                }


            }

            override fun onFailure(response: Response<ApiVersionResponse?>) {
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


    fun postFCMTokenToServer(request: FCMRegistrationRequest, context: Context?) {
        getInstance(context)

        apiInterface!!.sendFCMTokenToServer(request)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun onSuccess(
                    response: Response<LocationResponse?>
                ) {
                    super.onSuccess(response)
                    AFJUtils.writeLogs("***** FCM Token Added to Server ***")

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
                    super.onAPIError(error)
                    val exception = error
                    viewModelScope.launch {
                        mErrorsMsg.emit(exception)
                    }
                }


            })

    }


    fun getNotificationCount(context: Context?) {
        getInstance(context)
        var request = LoginRequest(deviceID = Constants.DEVICE_ID)
        apiInterface!!.getNotificationCount(request)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun onSuccess(
                    response: Response<LocationResponse?>
                ) {
                    super.onSuccess(response)
                    if (response.body()?.data?.notificationCount!! > 0) {
                        viewModelScope.launch {
                            _notificationCount.emit(response.body()?.data?.notificationCount ?: 0)
                        }

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
                    super.onAPIError(error)
                    val exception = error
                    viewModelScope.launch {
                        mErrorsMsg.emit(exception)
                    }

                }
            })
    }

}


interface OnUpdateNeededListener {
    fun onUpdateNeeded(updateUrl: String)
    fun onAppUptoDate(){}
}