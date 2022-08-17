package com.example.afjtracking.view.activity.viewmodel

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.databinding.FragmentDeviceFormBinding
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.responses.LoginResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.utils.AFJUtils
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



    fun getDeviceDetailDialog(view: View)
    {

        val builder = AlertDialog.Builder(view.context)
        val dialogBinding: FragmentDeviceFormBinding =
            FragmentDeviceFormBinding.inflate(
                LayoutInflater.from(view.context),
                null, false
            )
        val mView: View = dialogBinding.root
        dialogBinding.deviceInfo = AFJUtils.getDeviceDetail()


        builder.setCancelable(false)
        builder.setView(mView)
        builder.setNegativeButton(android.R.string.no, object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface, p1: Int) {
                dialog.dismiss()
            }

        })
        val alertDialog = builder.create()
        alertDialog.show()

    }


    fun onClick(view: View?) {

        var deviceData = AFJUtils.getDeviceDetail()





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