package com.example.afjtracking.service.worker.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.ErrorRequest
import com.example.afjtracking.model.requests.LocationApiRequest
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.room.model.TableLocation
import com.example.afjtracking.room.repository.LocationTableRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationServiceVM : ViewModel() {


    companion object {
        private var instance: LocationServiceVM? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): LocationServiceVM? {
            if (instance == null) instance = LocationServiceVM()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context, false).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }

    var apiCallCounter = 0
    lateinit var liveAPIData: LiveData<TableLocation>
    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()

    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }

    val apiRequestStatus = MutableLiveData<Boolean>()
    val getApiRequestStatus: LiveData<Boolean>
        get() = apiRequestStatus

    private val toastMsg = MutableLiveData<String?>()
    val showToastMsg: LiveData<String?>
        get() = toastMsg

    fun insertDataToTable(context: Context, tableData: TableLocation) {
        LocationTableRepo.insertLocationData(context, tableData)
    }

    fun updateApiDataValue(context: Context, tableData: TableLocation) {
        LocationTableRepo.updateLocationData(context, tableData)
    }

    fun getAllErrorData(context: Context): List<TableLocation> {
        val listUncompletedFiles = LocationTableRepo.getErrorResultsData(context)
        return listUncompletedFiles
    }

    fun getAllPendingRequest(context: Context): List<TableLocation> {
        val listUncompletedFiles = LocationTableRepo.getUnCompletedLocationRequest(context)
        return listUncompletedFiles
    }

    fun sendApiRequest(context: Context, tableAPIData: TableLocation) {
        getInstance(context)
        callLocationApi(context, tableAPIData)

    }

    fun callLocationApi(context: Context, apiRequest: TableLocation) {

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val requestBody = AFJUtils.convertStringToObject(apiRequest.apiPostData,LocationApiRequest::class.java)

                apiInterface!!.updateLocation(requestBody)
                    .enqueue(object : Callback<LocationResponse?> {
                        override fun onResponse(
                            call: Call<LocationResponse?>,
                            response: Response<LocationResponse?>
                        ) {
                            if (response.body() != null) {
                                if (response.body()!!.code == 200) {
                                    apiRequestStatus.postValue(true)
                                } else {
                                    var errors = ""
                                    for (i in response.body()!!.errors!!.indices) {
                                        errors = "$errors${response.body()!!.errors!![i].message}"
                                    }
                                    mErrorsMsg!!.postValue(errors)
                                }
                            } else {
                                mErrorsMsg!!.postValue(response.raw().message)
                            }
                        }

                        override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                            mErrorsMsg!!.postValue(t.toString())

                        }
                    })

            }
        }

    }

 fun uploadErrorData(context: Context, locationTable: TableLocation ) {

            getInstance(context)
            val body = ErrorRequest(
                deviceId = Constants.DEVICE_ID,
                endpoint = locationTable.apiName,
                error =    locationTable.apiError,
                retries =  locationTable.apiRetryCount.toString()

            )
            apiInterface!!.postErrorData(body).enqueue(object : Callback<LocationResponse?> {
                override fun onResponse(
                    call: Call<LocationResponse?>,
                    response: Response<LocationResponse?>
                ) {
                    locationTable.apiResponseTime = AFJUtils.getCurrentDateTime()

                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {
                            AFJUtils.writeLogs("********* Error Upload Data Completed *********")

                            locationTable.apiStatus = 0
                            locationTable.errorPosted = "1"

                            updateApiDataValue(context, locationTable)

                            AFJUtils.writeLogs("********* Backup Api Request Completed *********")


                        } else {
                            var errors = ""
                            for (i in response.body()!!.errors!!.indices) {
                                errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                            }
                            AFJUtils.writeLogs("********* Error Upload Data Api=$errors *********")
                        }
                    } else {
                        AFJUtils.writeLogs("********* Error Upload Data Api=${response.raw().message} *********")
                    }
                }

                override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                    AFJUtils.writeLogs("********* Error Upload Data Api=${t.toString()} *********")
                }
            })

        }

    }


