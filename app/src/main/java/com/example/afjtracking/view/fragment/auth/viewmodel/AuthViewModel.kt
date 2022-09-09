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
import com.google.gson.annotations.SerializedName
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


data class QRFireDatabase(val deviceId:String?="", val status:Boolean?=false, val expiresAt: String?="", val data:QRFirebaseData?=QRFirebaseData() )
data class QRFirebaseData(
    val token   : String?  = null,
    val user : QRFirebaseUser? = QRFirebaseUser()
)
data class QRFirebaseUser(

    val id                             : Int?    = null,
    val firstname                      : String? = null,
    val middlename                     : String? = null,
    val lastname                       : String? = null,
    val sage_id                        : String? = null,
    val national_insurance_number      : String? = null,
    val contact_no                     : String? = null,
    val official_email                 : String? = null,
    val personal_email                 : String? = null,
    val identity_no                    : String? = null,
    val date_of_birth                   : String? = null,
    val gender                         : String? = null,
    val emergency_contact_relationship : String? = null,
    val emergency_contact              : String? = null,
    val emergency_contact_address      : String? = null,
    val current_address                : String? = null,
    val permanent_address              : String? = null,
    val city                           : String? = null,
    val designation                    : String? = null,
    val type                           : String? = null,
    val status                         : Int?    = null,
    val employment_status              : String? = null,
    val employment_type                : String? = null,
    val picture                        : String? = null,
    val joining_date                   : String? = null,
    val exit_date                      : String? = null,
    val gross_salary                   : Int?    = null,
    val bonus                          : Int?    = null,
    val branch_id                      : Int?    = null,
    val department_id                  : String? = null,
    val device_type                    : String? = null,
    val deleted_at                     : String? = null,
    val created_at                     : String? = null,
    val updated_at                     : String? = null,
    val full_name                      : String? = null

)