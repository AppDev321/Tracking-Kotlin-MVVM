package com.example.afjtracking.view.fragment.fileupload.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.ErrorRequest
import com.example.afjtracking.model.requests.MultipartRequest
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.model.responses.UploadFileAPiResponse
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.room.model.TableUploadFile
import com.example.afjtracking.room.repository.FileUploadRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.fragment.fileupload.FileUploadProgressListener
import com.example.afjtracking.view.fragment.fileupload.UploadDialogListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FileUploadModel() : ViewModel() {


    companion object {
        private var instance: FileUploadModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): FileUploadModel? {
            if (instance == null) instance = FileUploadModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context, false).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }

    var liveDataFileUpload: LiveData<TableUploadFile>? = null
    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()

    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }

     val fileUploadedSuccessfull = MutableLiveData<Boolean>()
    val getFileUploadStatus: LiveData<Boolean>
        get() = fileUploadedSuccessfull

    private val toastMsg = MutableLiveData<String?>()
    val showToastMsg: LiveData<String?>
        get() = toastMsg


    lateinit var uploadJob: Call<UploadFileAPiResponse?>


    fun uploadFileApi(
        listner: FileUploadProgressListener,
        context: Context,
        fileUploadData: TableUploadFile,
        listnerUploadDialog: UploadDialogListener

    ) {


        getInstance(context)

        val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

        bodyBuilder.addFormDataPart("type", fileUploadData.type)
        bodyBuilder.addFormDataPart("filetype", fileUploadData.fileType)
        bodyBuilder.addFormDataPart("field_name", fileUploadData.fieldName)
        bodyBuilder.addFormDataPart("upload_id", fileUploadData.uploadID)


        val file= File(fileUploadData.filePath)
        //File
        bodyBuilder.addFormDataPart(
            "file",
            file.name,
            file.asRequestBody("${fileUploadData.fileType}/*".toMediaTypeOrNull())
        )
        //bodyBuilder.addFormDataPart("video", file.name, file.asRequestBody("video/*".toMediaTypeOrNull()))

        val requestBody = bodyBuilder.build()
        val requestBodyWithProgress = MultipartRequest(requestBody) { progress ->
            listner.onProgressUpdate(progress)

        }


        uploadJob = apiInterface!!.uploadFileApi(requestBodyWithProgress)
        uploadJob.enqueue(object : Callback<UploadFileAPiResponse?> {
            override fun onResponse(
                call: Call<UploadFileAPiResponse?>,
                response: Response<UploadFileAPiResponse?>
            ) {
                fileUploadData.apiResponseTime = AFJUtils.getCurrentDateTime()

                if (response.body() != null) {
                    if (response.body()!!.code == 200) {
                        listnerUploadDialog.onUploadCompleted(response.body()!!)
                        fileUploadedSuccessfull.postValue(true)

                    } else {
                        fileUploadedSuccessfull.postValue(false)
                        var errors = ""
                        for (i in response.body()!!.errors!!.indices) {
                            errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                        }
                        mErrorsMsg!!.postValue(errors)
                    }
                } else {
                    fileUploadedSuccessfull.postValue(false)
                    mErrorsMsg!!.postValue(response.raw().message)
                }
            }

            override fun onFailure(call: Call<UploadFileAPiResponse?>, t: Throwable) {
                mErrorsMsg!!.postValue(t.toString())
                fileUploadedSuccessfull.postValue(false)
                fileUploadData.apiResponseTime = AFJUtils.getCurrentDateTime()
            }
        })

    }


    fun insertDataToTable(context: Context, tableData: TableUploadFile) {
        FileUploadRepo.insertData(context, tableData)


    }

    fun updateFileData(context: Context,  tableData: TableUploadFile) {
        FileUploadRepo.updateFileData(context, tableData)
    }

    fun getFileUploadDetail(context: Context, fileId: String): LiveData<TableUploadFile>? {
        liveDataFileUpload = FileUploadRepo.getUploadFileDetails(context, fileId)
        return liveDataFileUpload
    }


    fun getFileDetail(context: Context, fileId: String): TableUploadFile {
        val liveDataFileUpload = FileUploadRepo.getFileDetail(context, fileId)
        return liveDataFileUpload
    }

    fun getAllUncompletedUploadFiles(context: Context): List<TableUploadFile> {
        val listUncompletedFiles = FileUploadRepo.getUncompletedFiles(context)
        return listUncompletedFiles
    }
    fun getAllError(context: Context): List<TableUploadFile> {
        val listUncompletedFiles = FileUploadRepo.getErrorData(context)
        return listUncompletedFiles
    }




    fun uploadErrorData(
         context: Context,
        fileUploadData: TableUploadFile

    ) {

        getInstance(context)
      val body=   ErrorRequest(
          deviceId = Constants.DEVICE_ID,
          endpoint = Constants.FILE_UPLOAD_API,
          error = fileUploadData.apiError,
          retries =  fileUploadData.apiRetryCount.toString()

      )

     apiInterface!!.postErrorData(body).enqueue(object : Callback<LocationResponse?> {
            override fun onResponse(
                call: Call<LocationResponse?>,
                response: Response<LocationResponse?>
            ) {
                fileUploadData.apiResponseTime = AFJUtils.getCurrentDateTime()

                if (response.body() != null) {
                    if (response.body()!!.code == 200) {

                        fileUploadData.errorPosted ="1"
                        updateFileData(context,fileUploadData)

                        AFJUtils.writeLogs("********* Error Upload Data Completed *********")
                    } else {
                        var errors = ""
                        for (i in response.body()!!.errors!!.indices) {
                            errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                        }
                        AFJUtils.writeLogs("********* Error Upload Data Api=$errors *********")
                    }
                } else {
                    AFJUtils.writeLogs("********* Error Upload Data Api=${response.raw().message} *********")
                }
            }

            override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                AFJUtils.writeLogs("********* Error Upload Data Api=${t.toString()} *********")
            }
        })

    }

}

