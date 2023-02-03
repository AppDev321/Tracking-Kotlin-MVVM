package com.afjltd.tracking.view.fragment.notification.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.LocationResponse
import com.afjltd.tracking.model.responses.NotificationDataResponse
import com.afjltd.tracking.model.responses.Notifications
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.utils.Constants
import com.afjltd.tracking.R
import retrofit2.Response

class NotificationViewModel : ViewModel() {

    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    private val _notification = MutableLiveData<List<Notifications>>()
    val notificationData :LiveData<List<Notifications>> = _notification


    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: NotificationViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): NotificationViewModel? {
            if (instance == null) instance = NotificationViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }



    fun getNotifications(context: Context) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getNotificationData(LoginRequest(deviceID = Constants.DEVICE_ID))
            .enqueue(object : SuccessCallback<NotificationDataResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onFailure(response: Response<NotificationDataResponse?>) {
                    super.onFailure(response)

                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onSuccess(

                    response: Response<NotificationDataResponse?>
                ) {
                    val data =response.body()!!.data!!.notifications
                    if(data.size >0)
                    _notification.postValue(data)
                    else
                        mErrorsMsg!!.postValue(context.resources.getString(R.string.no_data_found))

                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                    mErrorsMsg!!.postValue(exception)


                }


            })

    }

    fun updateNotificationStatus(context: Context?,notificationId:Int) {
        getInstance(context)
       // _dialogShow.postValue(true)
        apiInterface!!.updateNotificationStatus(LoginRequest(deviceID = Constants.DEVICE_ID, notificatonID = notificationId))
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)

                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                   // mErrorsMsg!!.postValue(errors)
                }

                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {


                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                   // mErrorsMsg!!.postValue(exception)
                }


            })

    }


    fun deleteNotification(context: Context?,notificationId:Int) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.deleteNotification(LoginRequest(deviceID = Constants.DEVICE_ID, notificatonID = notificationId))
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }
                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)

                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                   // mErrorsMsg!!.postValue(errors)
                }

                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {


                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                   // mErrorsMsg!!.postValue(exception)
                }


            })

    }

}