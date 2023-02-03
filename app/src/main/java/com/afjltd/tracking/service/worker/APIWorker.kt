package com.afjltd.tracking.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.afjltd.tracking.room.model.TableAPIData
import com.afjltd.tracking.service.worker.viewmodel.ApiServiceViewModel
import com.afjltd.tracking.utils.AFJUtils
import kotlinx.coroutines.*


class APIWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val context = appContext

    var listRemaingingApis: List<TableAPIData> = arrayListOf()
    val apiServiceModel = ApiServiceViewModel()



    override suspend fun doWork(): Result {

            sendApiDataToServer()
            uploadErrorToServer()

        return Result.success()
    }

    private fun sendApiDataToServer() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                listRemaingingApis = apiServiceModel.getAllPendingRequest(context)

                for (i in listRemaingingApis.indices) {
                    try {
                        if (listRemaingingApis[i].apiPostData.length > 0) {
                            AFJUtils.writeLogs("Service has data size = ${listRemaingingApis.size}")
                            sendRequestToServer(listRemaingingApis[i])
                        }
                    }
                    catch (e : Exception)
                    {
                        AFJUtils.writeLogs(e.toString())
                    }
                }
            }

        } catch (exeption: Exception) {
            AFJUtils.writeLogs(exeption.toString())

        }

    }

    private fun uploadErrorToServer() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                listRemaingingApis = apiServiceModel.getAllErrorData(context)
                for (i in listRemaingingApis.indices) {
                    uploadErrorDataToServer(listRemaingingApis[i])
                }
            }
        } catch (exeption: Exception) {
            AFJUtils.writeLogs(exeption.toString())
        }
    }


    fun sendRequestToServer(apiData: TableAPIData) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                apiServiceModel.sendApiRequest(
                    context = context,
                    tableAPIData = apiData
                )
                apiServiceModel.errorsMsg.observeForever()
                {
                    AFJUtils.writeLogs("  error_api_request= $it \n********* Error in Api Request *********")
                    apiData.apiStatus = 0
                    apiData.apiRetryCount = apiData.apiRetryCount + 1
                    apiData.apiError = it.toString()
                    apiData.lastTimeApiError = it.toString()
                    apiServiceModel.updateApiDataValue(context, apiData)

                }
                apiServiceModel.apiRequestStatus.observeForever()
                {
                    if (it) {
                        apiData.apiStatus = 1
                        apiData.apiError = ""
                        apiServiceModel.updateApiDataValue(context, apiData)
                        AFJUtils.writeLogs("********* Backup Api Request Completed *********")
                    }

                }
            }
        }

    }

    fun uploadErrorDataToServer(fileUpload: TableAPIData) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                apiServiceModel.uploadErrorData(
                    context = context,
                    fileUploadData = fileUpload
                )
            }
        }
    }
}