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
package com.example.afjtracking.utils


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
import com.example.afjtracking.R
import com.example.afjtracking.model.requests.DeviceDetail
import com.example.afjtracking.service.worker.APIWorker
import com.example.afjtracking.service.worker.LocationWorker
import com.example.afjtracking.service.worker.UploadWorker
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.GsonBuilder
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


object AFJUtils {
    const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates"
    const val KEY_USER_TOKEN = "user_token"
    const val KEY_VEHICLE_DETAIL = "vehicle_detail"
    const val KEY_USER_DETAIL = "user_detail"
    const val KEY_API_STATUS = "background_service_status"


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


    fun startInAnimation(context: Context,view : View)
    {
        view.startAnimation(
            setAnimation(
                R.anim.fade_in,
                AccelerateDecelerateInterpolator(),
                true,
                context
            )
        )
    }

    fun startOutAnimation(context: Context,view : View)
    {
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
    fun requestingLocationUpdates(context: Context?): Boolean {
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



    fun getAPICountStatus(context: Context): Int {
        return getPrefs(context)
            .getInt(KEY_API_STATUS, 0)
    }


    fun setAPICountStatus(context: Context, apiCount: Int) {
        getPrefs(context)
            .edit()
            .putInt(KEY_API_STATUS, apiCount)
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



    fun getPrefs(context: Context): SharedPreferences {
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
        Log.e("AFJ Logs", msg)
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

    fun convertServerDateTime(date: String,is24Hour :Boolean): String {
        val inputDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val sourceSdf = SimpleDateFormat(inputDateFormat, Locale.getDefault())
        if(is24Hour) {
            val requiredSdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            return requiredSdf.format(sourceSdf.parse(date))
        }
        else
        { val requiredSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return requiredSdf.format(sourceSdf.parse(date))
        }
    }

    fun getCurrentDateTime(): String {

        val sdf = SimpleDateFormat("dd-M-yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())


        return currentDate
    }

    fun getFileSizeInMB(file: File): String {
        val fileSize = file.length()

        val sizeInMb = fileSize / (1024.0 * 1024)

        val sizeInMbStr = "%.2f".format(sizeInMb)

        return "${sizeInMbStr} MB"
    }





    fun <T> convertObjectToJson(objectClass: T) : String {
        val gsonPretty = GsonBuilder()
            .setPrettyPrinting().create()
        return gsonPretty.toJson(objectClass)
    }

    fun <T> convertStringToObject( key: String,clazz: Class<T>): T {
        val gsonPretty = GsonBuilder()
            .setPrettyPrinting().create()
        return gsonPretty.fromJson(key,clazz)

    }


    fun getDeviceDetail():DeviceDetail
    {
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


        return deviceData
    }

}