package com.afjltd.tracking.view.fragment.home.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afjltd.tracking.model.requests.FCMRegistrationRequest
import com.afjltd.tracking.model.requests.LocationApiRequest
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.LocationResponse
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.room.model.TableLocation
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import retrofit2.Response

class TrackingViewModel : ViewModel() {

    private val _notificationCount = MutableLiveData<Int>()
    val notificationCount: LiveData<Int> = _notificationCount



    private var _locaitonRequest = MutableLiveData<LocationApiRequest>()

    val getLocationRequest: MutableLiveData<LocationApiRequest>
        get() {
            if (_locaitonRequest == null) {
                _locaitonRequest = MutableLiveData()
            }
            return _locaitonRequest
        }


    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()

    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


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
                    AFJUtils.writeLogs("Location API =${ response.body()!!.data!!.message.toString()}")
                    getLocationRequest.postValue(request)

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
                        mErrorsMsg!!.postValue(exception)

                    }
                }
            })

    }


    //Post FCM Token


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
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                    mErrorsMsg!!.postValue(exception)
                }



            })

    }



    fun getNotificationCount( context: Context?) {
        getInstance(context)
        var request = LoginRequest(deviceID = Constants.DEVICE_ID)
        apiInterface!!.getNotificationCount(request)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun onSuccess(
                    response: Response<LocationResponse?>
                ) {
                    super.onSuccess(response)
                   if( response.body()?.data?.notificationCount!! > 0)
                   {
                       _notificationCount .postValue( response.body()?.data?.notificationCount)
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
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                    mErrorsMsg!!.postValue(exception)
                }
            })
    }

}