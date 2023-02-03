package com.afjltd.tracking.view.activity.viewmodel

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afjltd.tracking.databinding.FragmentDeviceFormBinding
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.GetContactListResponse
import com.afjltd.tracking.model.responses.LoginResponse
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.utils.AFJUtils

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

fun onRetryClick(view:View)
{
    var deviceData = AFJUtils.getDeviceDetail()
    val loginUser = LoginRequest(deviceDetail= deviceData)

    userMutableLiveData!!.postValue(loginUser)
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
        val loginUser = LoginRequest(deviceDetail= deviceData)
        loginUser.strEmailAddress = EmailAddress.value
        loginUser.strPassword = Password.value
        userMutableLiveData!!.postValue(loginUser)
    }

    fun loginApiRequest(request: LoginRequest?, context: Context?) {
        getInstance(context)
        apiInterface!!.getVehicleData(request).enqueue(object : SuccessCallback<LoginResponse?>() {
                override fun onSuccess(
                    response: Response<LoginResponse?>
                ) {
                    super.onSuccess(response)
                    // mUserToken!!.postValue(response.body()!!.data!!.token!!)
                    mUserToken!!.postValue("0")


                    //Save LognigREsponse object
                    AFJUtils.saveObjectPref(
                        context!!,
                        AFJUtils.KEY_LOGIN_RESPONSE,
                        response.body()!!
                    )


                    //Save vehicle object
                    AFJUtils.saveObjectPref(
                        context,
                        AFJUtils.KEY_VEHICLE_DETAIL,
                        response.body()!!.data!!.vehicle
                    )
                    //Save User object
                    AFJUtils.saveObjectPref(
                        context,
                        AFJUtils.KEY_USER_DETAIL,
                        response.body()!!.data!!.user
                    )



                    getContactList(context)


                }
                override fun onFailure(response: Response<LoginResponse?>) {
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
                    mErrorsMsg!!.postValue(error)
                }
            })
    }

    fun getContactList( context: Context?) {
        getInstance(context)
        apiInterface!!.getContactList().enqueue(object : SuccessCallback<GetContactListResponse?>() {
            override fun onSuccess(
                response: Response<GetContactListResponse?>
            ) {
                super.onSuccess(response)


                AFJUtils.saveObjectPref(
                    context!!,
                    AFJUtils.KEY_CONTACT_LIST_PREF,
                    response.body()?.data
                )


            }
            override fun onFailure(response: Response<GetContactListResponse?>) {
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
                mErrorsMsg!!.postValue(error)
            }
        })
    }
}