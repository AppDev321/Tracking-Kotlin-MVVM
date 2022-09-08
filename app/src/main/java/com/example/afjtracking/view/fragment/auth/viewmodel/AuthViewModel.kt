package com.example.afjtracking.view.fragment.auth.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AuthViewModel : ViewModel() {
    @JvmField
    var EmailAddress = MutableLiveData<String>()

    @JvmField
    var Password = MutableLiveData<String>()


    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    private var userMutableLiveData: MutableLiveData<LoginRequest>? = null
    val user: MutableLiveData<LoginRequest>
        get() {
            if (userMutableLiveData == null) {
                userMutableLiveData = MutableLiveData()
            }
            return userMutableLiveData!!
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

   fun onLoginAuthClick(view:View)
    {}
}


data class QRFireDatabase(val deviceId:String?="", val status:Boolean?=false, val expiresAt: String?="", val data:Data?=Data() )