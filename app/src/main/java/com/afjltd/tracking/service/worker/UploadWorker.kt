

package com.afjltd.tracking.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.afjltd.tracking.model.responses.UploadFileAPiResponse
import com.afjltd.tracking.room.model.TableUploadFile
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.view.fragment.fileupload.FileUploadProgressListener
import com.afjltd.tracking.view.fragment.fileupload.UploadDialogListener
import com.afjltd.tracking.view.fragment.fileupload.viewmodel.FileUploadModel
import kotlinx.coroutines.*


class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val context = appContext

    var listFilesUpload: List<TableUploadFile> = arrayListOf()
    val fileViewModel = FileUploadModel()

    override suspend fun doWork(): Result {


     uploadFileToServer()
     uploadErrorToServer()

       // UploadUtil().uploadFileTask(context)
       // UploadUtil().uploadErrorToServer(context)

        return Result.success()
    }



    private fun uploadFileToServer() {
        try {
                CoroutineScope(Dispatchers.IO).launch {

                    listFilesUpload = fileViewModel.getAllUncompletedUploadFiles(context)

                    if(listFilesUpload.size > 0 ) {
                        AFJUtils.writeLogs("Upload Service has data size = ${listFilesUpload.size}")
                        for (i in listFilesUpload.indices) {
                            try {
                            uploadFileDataToServer(listFilesUpload[i])
                            }
                            catch (e :Exception)
                            {
                                AFJUtils.writeLogs(e.toString())
                            }
                        }
                    }


          }
        } catch (exeption: Exception) {
            AFJUtils.writeLogs(  exeption.toString())

        }
    }

    private fun uploadErrorToServer() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                listFilesUpload = fileViewModel.getAllError(context)
                for(i in listFilesUpload.indices)
                {
                    try {
                        uploadErrorDataToServer(listFilesUpload[i])
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


    fun uploadFileDataToServer(fileUpload: TableUploadFile)
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

                  /*  NotificationUtils.showNotification(context,"AFJ File Uploaded",
                        "File uploaded completed","")*/

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

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
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
        }

    }


    fun uploadErrorDataToServer(fileUpload: TableUploadFile)
    {
        fileViewModel.uploadErrorData(
            context = context,
            fileUploadData = fileUpload
        )
    }


}