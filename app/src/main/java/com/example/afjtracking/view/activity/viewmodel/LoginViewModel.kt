package com.example.afjtracking.view.activity.viewmodel

import android.content.Context
import android.os.Build
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.DeviceDetail
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.responses.LoginResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {
    @JvmField
    var EmailAddress = MutableLiveData<String>()
    @JvmField
    var Password = MutableLiveData<String>()

    @JvmField
    var vrnNumber = MutableLiveData<String>()


    private var userMutableLiveData: MutableLiveData<LoginRequest>? = null
    private var mUserToken: MutableLiveData<String>? = MutableLiveData()
    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()

    companion object {
        private var instance: LoginViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): LoginViewModel? {
            if (instance == null) instance = LoginViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    val userToken: MutableLiveData<String>
        get() {
            if (mUserToken == null) {
                mUserToken = MutableLiveData()
            }
            return mUserToken!!
        }
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }
    val user: MutableLiveData<LoginRequest>
        get() {
            if (userMutableLiveData == null) {
                userMutableLiveData = MutableLiveData()
            }
            return userMutableLiveData!!
        }

    fun onClick(view: View?) {

        var deviceData = DeviceDetail()
        deviceData.brand = Build.BRAND
        deviceData.model = Build.MODEL
        deviceData.androidVersion = Build.VERSION.RELEASE
        deviceData.deviceID = Constants.DEVICE_ID
        /*deviceData.macBluetooth = android.provider.Settings.Secure.getString( view!!.context.contentResolver, "bluetooth_address")
        val telephonyManager = view!!.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        deviceData.imeiSim = if (Build.VERSION.SDK_INT >= 26) {

               try{
                   telephonyManager.imei
               }catch (e :Exception)
               {
                   deviceData.deviceID
               }

            } else {
                telephonyManager.deviceId
            }
*/






        val loginUser = LoginRequest(
            EmailAddress.value.toString(),
            Password.value.toString(),
            vrnNumber.value.toString() ,
            deviceData
        )





        userMutableLiveData!!.postValue(loginUser)
    }

    fun loginApiRequest(request: LoginRequest?, context: Context?) {
        getInstance(context)
        apiInterface!!.getLoginUser(request).enqueue(object : Callback<LoginResponse?> {
                override fun onResponse(
                    call: Call<LoginResponse?>,
                    response: Response<LoginResponse?>
                ) {
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {
                            mUserToken!!.postValue(response.body()!!.data!!.token!!)

                            //Save vehicle object
                            AFJUtils.saveObjectPref(
                                context!!,
                                AFJUtils.KEY_VEHICLE_DETAIL,
                                response.body()!!.data!!.vehicle
                            )


                            //Save User object
                            AFJUtils.saveObjectPref(
                                context!!,
                                AFJUtils.KEY_USER_DETAIL,
                                response.body()!!.data!!.user
                            )


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

                override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                    mErrorsMsg!!.postValue(t.toString())
                }
            })
    }


}