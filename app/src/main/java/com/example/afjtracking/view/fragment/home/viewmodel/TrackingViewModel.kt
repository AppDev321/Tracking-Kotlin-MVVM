package com.example.afjtracking.view.fragment.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.FCMRegistrationRequest
import com.example.afjtracking.model.requests.LocationApiRequest
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.room.model.TableLocation
import com.example.afjtracking.room.repository.LocationTableRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private var _locaitonRequest = MutableLiveData<LocationApiRequest>()

    val getLocationRequest: MutableLiveData<LocationApiRequest>
        get() {
            if (_locaitonRequest == null) {
                _locaitonRequest = MutableLiveData()
            }
            return _locaitonRequest!!
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
            .enqueue(object : Callback<LocationResponse?> {
                override fun onResponse(
                    call: Call<LocationResponse?>,
                    response: Response<LocationResponse?>
                ) {
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {
                            Log.e("Location API", response.body()!!.data!!.message.toString())

                            getLocationRequest.postValue(request)

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

                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        var locationTable = TableLocation(
                            apiName =  Constants.LOCATION_API,
                            apiPostData =  AFJUtils.convertObjectToJson(request!!),
                            apiPostResponse = "",
                            apiError = "",
                            apiRetryCount = 0,
                            lastTimeApiError = "",
                            apiRequestTime = AFJUtils.getCurrentDateTime(),
                            apiResponseTime = "",
                            errorPosted = "0"

                        )
                        LocationTableRepo.insertLocationData(context!!, locationTable)
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
            .enqueue(object : Callback<LocationResponse?> {
                override fun onResponse(
                    call: Call<LocationResponse?>,
                    response: Response<LocationResponse?>
                ) {
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {
                            AFJUtils.writeLogs("***** FCM Token Added to Server ***")
                        }
                        else {
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

}