package com.afjltd.tracking.view.fragment.route.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.afjltd.tracking.broadcast.TrackingAppBroadcast
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.GetRouteListResponse
import com.afjltd.tracking.model.responses.Sheets
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.service.location.ForegroundLocationService
import com.afjltd.tracking.service.location.LocationRepository
import com.afjltd.tracking.utils.AFJUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response


class RouteViewModel : ViewModel() {

    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    val _routeList = MutableLiveData<List<Sheets>>()
    val getRouteList: LiveData<List<Sheets>> = _routeList


    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: RouteViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): RouteViewModel? {
            if (instance == null) instance = RouteViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getRouteList(context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.fetchRouteList(LoginRequest(deviceDetail =AFJUtils.getDeviceDetail()))
            .enqueue(object : SuccessCallback<GetRouteListResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<GetRouteListResponse?>
                ) {
                    super.onSuccess(response)

                    val res = response.body()?.data?.sheets ?: arrayListOf()
                    if (res.size > 0) {
                        _routeList.postValue(res)
                    } else {
                        mErrorsMsg!!.postValue("No Route Found")
                    }
                }

                override fun onFailure(response: Response<GetRouteListResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onAPIError(t: String) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    mErrorsMsg!!.postValue(exception)


                }
            })

    }


    fun updateRouteListStatus(context: Context?,request: LoginRequest,responseCallback:(Any)->Unit) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.updateRouteStatus(request)
            .enqueue(object : SuccessCallback<GetRouteListResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<GetRouteListResponse?>
                ) {
                    super.onSuccess(response)
                    var res = response.body()
                    responseCallback("Success")

                }
                override fun onFailure(response: Response<GetRouteListResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    responseCallback(errors)
                }
                override fun onAPIError(t: String) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    responseCallback(exception)

                }
            })

    }



}