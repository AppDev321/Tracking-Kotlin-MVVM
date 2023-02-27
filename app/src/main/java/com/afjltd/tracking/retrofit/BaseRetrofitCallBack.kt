package com.afjltd.tracking.retrofit

import com.afjltd.tracking.utils.AFJUtils
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


internal abstract class BaseRetrofitCallBack<T> {
    abstract fun onSuccess(response: Response<T>)
    abstract fun onFailure(response: Response<T>)
    abstract fun onAPIError(error: String)
    abstract fun loadingDialog(show: Boolean)
}

internal abstract class SuccessCallback<T> : BaseRetrofitCallBack<T>(), Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        loadingDialog(false)
        try {
            if (response.code() in 400..598) {
                if (response.body() == null) {
                    when (response.code()) {
                        405 -> {
                            onAPIError("Method Not Allowed")
                        }
                        404 -> {
                            onAPIError("URL Not Found")
                        }
                        500 -> {
                            onAPIError("Internal Server Error")
                        }

                        else -> {
                            // onAPIError(response.errorBody().toString())
                            onAPIError(response.raw().message)
                        }
                    }
                    return
                } else {
                    onFailure(response)
                }
            } else {
                try {
                    val jsonObject = JSONObject(Gson().toJson(response.body()))
                    val responseCode = jsonObject.getInt("code")
                    if (responseCode == 200) {
                        onSuccess(response)
                    } else {
                        onFailure(response)
                    }
                }catch (e:java.lang.Exception)
                {
                    AFJUtils.writeLogs("Exception faced on Base Retrofit response code fetching")
                }

            }
        }
        catch (e :Exception)
        {
            AFJUtils.writeLogs("Exception faced on Base Retrofit response")
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        loadingDialog(false)
        if(t.toString().contains("java.net.UnknownHostException"))
        {
            onAPIError(t.toString())
        }
        else
        {
            onAPIError("There is some issue please try again")
        }

    }

    override fun onSuccess(response: Response<T>) {}
    override fun onFailure(response: Response<T>) {

    }
    override fun onAPIError(error: String) {}
    override fun loadingDialog(show: Boolean) {}
}