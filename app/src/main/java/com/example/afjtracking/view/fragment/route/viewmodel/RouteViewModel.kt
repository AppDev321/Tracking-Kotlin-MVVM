package com.example.afjtracking.view.fragment.route.viewmodel

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.R
import com.example.afjtracking.model.requests.DeviceDetail
import com.example.afjtracking.model.requests.FormRequest
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.repository.ApiDataRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.TextAnalyser
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.text.DecimalFormat
import java.util.regex.Matcher
import java.util.regex.Pattern


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