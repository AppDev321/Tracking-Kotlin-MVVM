package com.example.afjtracking.crashhandler

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    var mApplication: Application? = null
    var mMyActivityLifecycleCallbacks = MyActivityLifecycleCallbacks()
    private val infos: MutableMap<String?, String?> = HashMap<String?, String?>()
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    private var mIsDebug = false
    private var mIsRestartApp = false
    private var mRestartTime: Long = 0
    private var mClassOfFirstActivity: Class<*>? = null
    private var hasToast = false
    fun init(
        application: Application,
        isDebug: Boolean,
        isRestartApp: Boolean,
        restartTime: Long,
        classOfFirstActivity: Class<*>?
    ) {
        mIsRestartApp = isRestartApp
        mRestartTime = restartTime
        mClassOfFirstActivity = classOfFirstActivity
        initCrashHandler(application, isDebug)
    }

    fun init(application: Application, isDebug: Boolean) {
        initCrashHandler(application, isDebug)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun initCrashHandler(application: Application, isDebug: Boolean) {
        mIsDebug = isDebug
        mApplication = application
        mApplication!!.registerActivityLifecycleCallbacks(mMyActivityLifecycleCallbacks)
        Thread.setDefaultUncaughtExceptionHandler(this)
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        val stacktrace = getCrashInfo(ex)
        if (stacktrace.contains("cc.cloudist.acplibrary.views")) {
            return
        }


        val isHandle = handleException(ex)
        if (!isHandle && mDefaultHandler != null) {
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            try {
               sendCrashToDataBase(mApplication, stacktrace)
              //  Thread.sleep(threadSleepTime)
            } catch (e: InterruptedException) {
                Log.e(TAG, "uncaughtException() InterruptedException:$e")
            }

        }
    }

    private fun handleException(ex: Throwable?): Boolean {
        if (!hasToast) {
            Thread {
                try {
                    Looper.prepare()
                    val toast: Toast?
                    toast = if (mCustomToast == null) {
                        Toast.makeText(mApplication, mCrashTip, Toast.LENGTH_LONG)
                        //toast.setGravity(Gravity.CENTER, 0, 0);
                    } else {
                        mCustomToast
                    }
                    toast!!.show()
                    Looper.loop()
                    hasToast = true
                } catch (e: Exception) {
                    Log.e(TAG, "handleException Toast error$e")
                }
            }.start()
        }
        return ex != null
    }

    fun collectDeviceInfo() {
        try {
            val pm = mApplication!!.packageManager
            val pi = pm.getPackageInfo(mApplication!!.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                TAG,
                "collectDeviceInfo() an error occured when collect package info NameNotFoundException:"
            )
        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field[null].toString()
                Log.i(TAG, field.name + " : " + field[null])
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "collectDeviceInfo() an error occured when collect crash info Exception:"
                )
            }
        }
    }

    private fun saveCatchInfo2File(ex: Throwable): String? {
        val sb = StringBuffer()
        sb.append("------------------------start------------------------------\n")
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }
        sb.append(getCrashInfo(ex))
        sb.append("\n------------------------end------------------------------")
        try {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date())
            val fileName = "crash-$time-$timestamp.txt"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val path =
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + "crash/"
                val dir = File(path)
                if (!dir.exists()) dir.mkdirs()
                if (!dir.exists()) dir.createNewFile()
                val fos = FileOutputStream(path + fileName)
                fos.write(sb.toString().toByteArray())
                LogcatCrashInfo(path + fileName)
                fos.close()
            }
            return fileName
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "saveCatchInfo2File() an error occured while writing file... Exception:")
        }
        return null
    }

    private fun LogcatCrashInfo(fileName: String) {
        if (!File(fileName).exists()) {
            Log.e(TAG, "LogcatCrashInfo()")
            return
        }
        var fis: FileInputStream? = null
        var reader: BufferedReader? = null
        var s: String? = null
        try {
            fis = FileInputStream(fileName)
            reader = BufferedReader(InputStreamReader(fis, "GBK"))
            while (true) {
                s = reader.readLine()
                if (s == null) break
                Log.e(TAG, s)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                reader!!.close()
                fis!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getCrashInfo(ex: Throwable): String {
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        ex.stackTrace = ex.stackTrace
        ex.printStackTrace(printWriter)
        printWriter.close()
        return result.toString()
    }

    private fun writeDataIntoFile(sBody: String?) {
        val formatter = SimpleDateFormat(Constants.dateFormat)
        val newFormatter = SimpleDateFormat(Constants.dateTimeFromat)
        val now = Date()
        val fileName = formatter.format(now) + "_new.txt" //like 2016_01_12.txt
        try {
            val root: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        .toString() + "/" + folderName
                )
            } else {
                File(Environment.getExternalStorageDirectory().toString() + "/Report Files FPE")
            }
            if (!root.exists()) {
                root.mkdirs()
            }
            val gpxfile = File(root, fileName)
            val writer = FileWriter(gpxfile, true)
            writer.append(newFormatter.format(now)).append(" => ").append(sBody).append("\n\n")
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun sendCrashToDataBase(context: Context?, crash: String?) {
      //  Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show()
        AFJUtils.writeLogs("AppCrash:$crash")

    }

    companion object {
        const val TAG = "CrashHandler"
        private var mCustomToast: Toast? = null
        private var mCrashTip = "Something went wrong"
        private var mCrashHandler: CrashHandler? = null
        const val folderName = "Report Files FPE"
        private const val threadSleepTime: Long = 800
        val instance: CrashHandler?
            get() {
                if (mCrashHandler == null) mCrashHandler = CrashHandler()
                return mCrashHandler
            }

        fun setCloseAnimation(closeAnimation: Int) {
            MyActivityLifecycleCallbacks.sAnimationId = closeAnimation
        }

        fun setCustomToast(customToast: Toast?) {
            mCustomToast = customToast
        }

        fun setCrashTip(crashTip: String) {
            mCrashTip = crashTip
        }
    }
}