package com.afjltd.tracking.utils

import android.content.Context
import com.afjltd.tracking.model.responses.UploadFileAPiResponse
import com.afjltd.tracking.room.model.TableUploadFile
import com.afjltd.tracking.view.fragment.fileupload.FileUploadProgressListener
import com.afjltd.tracking.view.fragment.fileupload.UploadDialogListener
import com.afjltd.tracking.view.fragment.fileupload.viewmodel.FileUploadModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext



//Currently not in use
class UploadUtil: CoroutineScope {
    private val fileViewModel = FileUploadModel()
    private lateinit var backgroundExecutionListener: BackgroundExecutionListener
    var listFilesUpload: List<TableUploadFile> = arrayListOf()

    fun withBackgroundExecutionListener(backgroundExecutionListener: BackgroundExecutionListener): UploadUtil {
        this.backgroundExecutionListener = backgroundExecutionListener
        return this
    }

    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + handler

    fun uploadFileTask(context : Context){
        listFilesUpload = fileViewModel.getAllUncompletedUploadFiles(context = context)

        if(listFilesUpload.size > 0 ) {
            AFJUtils.writeLogs("Upload Service has data size = ${listFilesUpload.size}")
            for (i in listFilesUpload.indices) {
                try {
                    uploadFileDataToServer(listFilesUpload[i],context)
                }
                catch (e :Exception)
                {
                    AFJUtils.writeLogs(e.toString())
                }
            }
        }
        

    }

    fun uploadBackgroundTask() = launch(Dispatchers.Main) {
        if (::backgroundExecutionListener.isInitialized)
        backgroundExecutionListener.onPre()
        withContext(Dispatchers.IO) {
         //   uploadFileTask()
        }.let {
            if (::backgroundExecutionListener.isInitialized)
            backgroundExecutionListener.onPost()
        }




    }



    fun uploadFileDataToServer(fileUpload: TableUploadFile,context :Context)
    {

        fileViewModel.uploadFileApi(
            context = context,
            fileUploadData = fileUpload,
            listnerUploadDialog = (object : UploadDialogListener {
                override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                    fileUpload.uploadStatus =1
                    fileUpload.apiError = ""
                    //Update data in table
                    fileViewModel.updateFileData(context,fileUpload)

                    NotificationUtils.showTextImageNotification(context,"AFJ File Uploaded",
                        "File uploaded completed","")

                    AFJUtils.writeLogs("********* File Upload Completed *********")
                }

                override fun onFilePathReceived(path: String) {

                }

            }),

            listner = (object : FileUploadProgressListener {
                override fun onProgressUpdate(progress: Int) {
                }
            }),
        )


                fileViewModel.errorsMsg.observeForever()
                {
                    AFJUtils.writeLogs("  error_file_upload= $it \n********* Error file Upload *********")

                    fileUpload.uploadStatus  = 0
                    fileUpload.apiRetryCount = fileUpload.apiRetryCount + 1
                    fileUpload.apiError = it.toString()
                    fileUpload.lastTimeApiError = it.toString()
                    fileViewModel.updateFileData(context,fileUpload)

                }



    }

     fun uploadErrorToServer(context:Context) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                listFilesUpload = fileViewModel.getAllError(context)
                for(i in listFilesUpload.indices)
                {
                    try {
                        uploadErrorDataToServer(listFilesUpload[i],context)
                    }
                    catch (e :Exception)
                    {
                        AFJUtils.writeLogs(e.toString())
                    }
                }
            }
        } catch (exeption: Exception) {
            AFJUtils.writeLogs(  exeption.toString())

        }
    }
    fun uploadErrorDataToServer(fileUpload: TableUploadFile,context:Context)
    {
        fileViewModel.uploadErrorData(
            context = context,
            fileUploadData = fileUpload
        )
    }

}

interface BackgroundExecutionListener{
    fun onPre()
    fun onPost()
}