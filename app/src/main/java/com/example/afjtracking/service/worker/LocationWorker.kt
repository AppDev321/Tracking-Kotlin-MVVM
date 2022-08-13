package com.example.afjtracking.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.afjtracking.room.model.TableLocation
import com.example.afjtracking.service.worker.viewmodel.LocationServiceVM
import com.example.afjtracking.utils.AFJUtils
import kotlinx.coroutines.*


class LocationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val context = appContext

    var listRemaingingApis: List<TableLocation> = arrayListOf()
    val apiServiceModel = LocationServiceVM()



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
                            sendRequestToServer(listRemaingingApis[i])
                        }
                    }
                    catch (e:Exception)
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


    fun sendRequestToServer(apiData: TableLocation) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                apiServiceModel.sendApiRequest(

                    context = context,
                    tableAPIData = apiData
                )
                apiServiceModel.errorsMsg.observeForever()
                {
                    AFJUtils.writeLogs("  error_api_request= $it \n********* Error in Location Request *********")

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

                        //Update data in table
                        apiServiceModel.updateApiDataValue(context, apiData)

                        AFJUtils.writeLogs("********* Backup Location Request Completed *********")
                    }

                }
            }
        }

    }

    fun uploadErrorDataToServer(location: TableLocation) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                apiServiceModel.uploadErrorData( context , location  )
            }
        }
    }
}