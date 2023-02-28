/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afjltd.tracking.utils


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AnimRes
import androidx.core.content.FileProvider
import androidx.work.*
import com.afjltd.tracking.BuildConfig
import com.afjltd.tracking.model.requests.DeviceDetail
import com.afjltd.tracking.service.worker.APIWorker
import com.afjltd.tracking.service.worker.LocationWorker
import com.afjltd.tracking.service.worker.UploadWorker
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.GsonBuilder
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.afjltd.tracking.R

object AFJUtils {
    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates"
    private const val KEY_USER_TOKEN = "user_token"
    const val KEY_VEHICLE_DETAIL = "vehicle_detail"
    const val KEY_USER_DETAIL = "user_detail"
    const val KEY_LOGIN_RESPONSE = "login_response"
    const val KEY_LOCATION_REQUEST_OBJECT = "location_request_object"
    const val KEY_CONTACT_LIST_PREF = "contact_list_pref"
    private const val KEY_LOCATION_RECEVIER = "location_receiver_register"


    enum class NOTIFICATIONTYPE{
        TEXT,
        IMAGE,
        LOCATION,
        EVENT,
        CALLING
    }



    enum class UI_TYPE{
        TEXT,
        IMAGE,
        FILE,
        MULTILINE,
        OPTION,
        MULTISELECT,
        DATETIME,
        TIME,
        DATE,
        RADIO
    }



    fun setAnimation(
        @AnimRes id: Int,
        interpolator: Interpolator?,
        fillAfter: Boolean,
        context: Context
    ): Animation? {

        val animation: Animation = AnimationUtils.loadAnimation(context, id)
        if (interpolator != null) {
            animation.interpolator = interpolator
        } else {
            animation.interpolator = LinearInterpolator()
        }
        animation.fillAfter = fillAfter
        return animation
    }


    fun startInAnimation(context: Context, view: View) {
        view.startAnimation(
            setAnimation(
                R.anim.fade_in,
                AccelerateDecelerateInterpolator(),
                true,
                context
            )
        )
    }

    fun startOutAnimation(context: Context, view: View) {
        view.startAnimation(
            setAnimation(
                R.anim.fade_in,
                AccelerateDecelerateInterpolator(),
                true,
                context
            )
        )
    }

    @JvmStatic
    fun getRequestingLocationUpdates(context: Context?): Boolean {
        return getPrefs(context!!)
            .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
    }

    @JvmStatic
    fun setRequestingLocationUpdates(context: Context?, requestingLocationUpdates: Boolean) {
        getPrefs(context!!)
            .edit()
            .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
            .apply()
    }

    @JvmStatic
    fun getLocationText(location: Location?): String {
        return if (location == null) "Unknown location" else "(" + location.latitude + ", " + location.longitude + ")"
    }

    @JvmStatic
    fun getLocationTitle(context: Context): String {
        return context.getString(
            R.string.location_updated,
            DateFormat.getDateTimeInstance().format(Date())
        )
    }

    @JvmStatic
    fun getUserToken(context: Context?): String? {
        return getPrefs(context!!)
            .getString(KEY_USER_TOKEN, "")
    }


    fun setUserToken(context: Context?, usertoken: String?) {
        getPrefs(context!!)
            .edit()
            .putString(KEY_USER_TOKEN, usertoken)
            .apply()
    }


    fun isLocationReceiverRegister(context: Context): Boolean {
        return getPrefs(context)
            .getBoolean(KEY_LOCATION_RECEVIER, false)
    }


    fun setLocationReceiverRegister(context: Context, status: Boolean) {
        getPrefs(context)
            .edit()
            .putBoolean(KEY_LOCATION_RECEVIER, status)
            .apply()
    }

    fun getScreenWidth(activity: Activity): Float {
        val display: Display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        return outMetrics.widthPixels.toFloat()
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            context.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
    }

    fun <T> saveObjectPref(context: Context, key: String, objectClass: T) {
        val editor: SharedPreferences.Editor = getPrefs(context).edit()
        val gson = GsonBuilder().create().toJson(objectClass)
        editor.putString(key, gson)
        editor.apply()
    }

    fun <T> getObjectPref(context: Context, key: String, clazz: Class<T>?): T {
        return GsonBuilder().create().fromJson(getPrefs(context).getString(key, null), clazz)
    }


    fun writeLogs(msg: String) {
     if (BuildConfig.DEBUG) {
            Log.e("AFJ Logs", msg)
     }
    }

    fun launchFileIntent(url: String, context: Context) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", File(url))
        //   val uri: Uri = Uri.fromFile(File(url))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION.or(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (url.contains(".doc") || url.contains(".docx")) {

            intent.setDataAndType(uri, "application/msword")
        } else if (url.contains(".pdf")) {

            intent.setDataAndType(uri, "application/pdf")
        } else if (url.contains(".ppt") || url.contains(".pptx")) {

            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.contains(".xls") || url.contains(".xlsx")) {

            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.contains(".rtf")) {

            intent.setDataAndType(uri, "application/rtf")
        } else if (url.contains(".jpg") || url
                .contains(".jpeg") || url.toString().contains(".png")
        ) {

            intent.setDataAndType(uri, "image/jpeg")
        } else if (url.contains(".txt")) {

            intent.setDataAndType(uri, "text/plain")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        context.startActivity(Intent.createChooser(intent, "Select Application"))
    }

    const val DOC = "application/msword"
    const val DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    const val IMAGE = "image/*"
    const val AUDIO = "audio/*"
    const val VIDEO = "video/*"
    const val TEXT = "text/*"
    const val PDF = "application/pdf"
    const val XLS = "application/vnd.ms-excel"

    fun getCustomFileChooserIntent(vararg types: String): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, types)
        return intent
    }


    fun setPeriodicWorkRequest(context: Context) {


        val constrains = Constraints.Builder()
            // .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED).build()


        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(
                UploadWorker::class.java,
                Constants.WORKER_SERVICE_TIME,
                Constants.TIME_WORKER_SERVICE_UNIT
            ).setConstraints(constrains)
            .build()

        val apiPeriodicWorkRequest = PeriodicWorkRequest
            .Builder(
                APIWorker::class.java,
                Constants.WORKER_SERVICE_TIME,
                Constants.TIME_WORKER_SERVICE_UNIT
            ).setConstraints(constrains)
            .build()

        val locationPeriodicWorkRequest = PeriodicWorkRequest
            .Builder(
                LocationWorker::class.java,
                Constants.WORKER_SERVICE_TIME,
                Constants.TIME_WORKER_SERVICE_UNIT
            ).setConstraints(constrains)
            .build()


        /* WorkManager.getInstance(context).enqueue(periodicWorkRequest)
      WorkManager.getInstance(context).enqueue(apiPeriodicWorkRequest)
       WorkManager.getInstance(context).enqueue(locationPeriodicWorkRequest)
 */

        //  if(isWorkEverScheduledBefore(context,"uploadFile")) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "uploadFile",
            ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest
        )

        //   }

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "apiData",
            ExistingPeriodicWorkPolicy.REPLACE, apiPeriodicWorkRequest
        )

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "location",
            ExistingPeriodicWorkPolicy.REPLACE, locationPeriodicWorkRequest
        )

    }


    private fun isWorkEverScheduledBefore(context: Context, tag: String): Boolean {
        val instance = WorkManager.getInstance(context)
        val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosForUniqueWork(tag)
        var workScheduled = false
        statuses.get()?.let {
            for (workStatus in it) {
                workScheduled = (
                        workStatus.state == WorkInfo.State.ENQUEUED
                                || workStatus.state == WorkInfo.State.RUNNING
                                || workStatus.state == WorkInfo.State.BLOCKED
                                || workStatus.state.isFinished // It checks SUCCEEDED, FAILED, CANCELLED already
                        )
            }
        }
        return workScheduled
    }


    fun setOneTimeWorkRequest(context: Context) {

        val constrains = Constraints.Builder()
            //  .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED).build()

        val workerRequest =
            OneTimeWorkRequest
                .Builder(UploadWorker::class.java)
                .setConstraints(constrains)
                .build()

        WorkManager.getInstance(context).enqueue(workerRequest)

    }

    fun convertServerDateTime(date: String, is24Hour: Boolean): String {
        val inputDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val sourceSdf = SimpleDateFormat(inputDateFormat, Locale.getDefault())
        if (is24Hour) {
            val requiredSdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            return sourceSdf.parse(date).let { requiredSdf.format(it!!) }
        } else {
            val requiredSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sourceSdf.parse(date).let { requiredSdf.format(it!!) }
        }
    }



    fun dateComparison(date: String,withTime :Boolean): Boolean {
        try {
            val inputDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            val sourceSdf = SimpleDateFormat(inputDateFormat, Locale.getDefault())
            if (withTime) {
                // val requiredSdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                val compareDateTime = uTCToLocal(inputDateFormat,inputDateFormat,date)?.let {
                    sourceSdf.parse(
                        it
                    )
                }
                val currentDateTime = sourceSdf.parse(sourceSdf.format(Date()))
                if (currentDateTime != null) {
                    when {
                        currentDateTime.before(compareDateTime) -> {
                            return true
                        }
                        currentDateTime.after(compareDateTime) -> {
                            return false
                        }
                        currentDateTime.after(compareDateTime) -> {
                            return false
                        }
                    }
                }
            } else {
                val requiredSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sourceSdf.parse(date).let {
                    if (it != null) {
                        requiredSdf.format(it)
                    }
                }
            }
        }
        catch (e:Exception){
                writeLogs("Date Parsing issuee .....")
        }
       return false
    }

    fun localToUTC(dateFormat: String?, datesToConvert: String?): String? {
        var dateToReturn = datesToConvert
        val sdf = SimpleDateFormat(dateFormat)
        sdf.timeZone = TimeZone.getDefault()
        var gmt: Date? = null
        val sdfOutPutToSend = SimpleDateFormat(dateFormat)
        sdfOutPutToSend.timeZone = TimeZone.getTimeZone("UTC")
        try {
            gmt = sdf.parse(datesToConvert)
            dateToReturn = sdfOutPutToSend.format(gmt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateToReturn
    }


    fun uTCToLocal(
        dateFormatInPut: String?,
        dateFomratOutPut: String?,
        datesToConvert: String?
    ): String? {
        var dateToReturn = datesToConvert
        val sdf = SimpleDateFormat(dateFormatInPut)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        var gmt: Date? = null
        val sdfOutPutToSend = SimpleDateFormat(dateFomratOutPut)
        sdfOutPutToSend.timeZone = TimeZone.getDefault()
        try {
            gmt = sdf.parse(datesToConvert)
            dateToReturn = sdfOutPutToSend.format(gmt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return dateToReturn
    }
    fun getCurrentDateTime(): String {

        val sdf = SimpleDateFormat("dd-M-yyyy hh:mm:ss")


        return sdf.format(Date())
    }

    fun getFileSizeInMB(file: File): String {
        val fileSize = file.length()

        val sizeInMb = fileSize / (1024.0 * 1024)

        val sizeInMbStr = "%.2f".format(sizeInMb)

        return "$sizeInMbStr MB"
    }


    fun <T> convertObjectToJson(objectClass: T): String {
        val gsonPretty = GsonBuilder()
            .setPrettyPrinting().create()
        return gsonPretty.toJson(objectClass)
    }

    fun <T> convertStringToObject(key: String, clazz: Class<T>): T {
        val gsonPretty = GsonBuilder()
            .setPrettyPrinting().create()
        return gsonPretty.fromJson(key, clazz)

    }



    fun getDeviceDetail(): DeviceDetail {
        val deviceData = DeviceDetail()
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


        return deviceData
    }
    fun getGreetingMessage():String{
        val c = Calendar.getInstance()

        return when (c.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..15 -> "Good Afternoon"
            in 16..20 -> "Good Evening"
            in 21..23 -> "Good Night"
            else -> "Hello"
        }
    }
     fun getDirection(angle: Double): String {
        var direction = ""

        if (angle >= 350 || angle <= 10)
            direction = "N"
        if (angle < 350 && angle > 280)
            direction = "NW"
        if (angle <= 280 && angle > 260)
            direction = "W"
        if (angle <= 260 && angle > 190)
            direction = "SW"
        if (angle <= 190 && angle > 170)
            direction = "S"
        if (angle <= 170 && angle > 100)
            direction = "SE"
        if (angle <= 100 && angle > 80)
            direction = "E"
        if (angle <= 80 && angle > 10)
            direction = "NE"

        return direction
    }
}