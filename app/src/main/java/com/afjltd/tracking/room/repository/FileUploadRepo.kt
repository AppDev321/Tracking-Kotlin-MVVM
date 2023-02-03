package com.afjltd.tracking.room.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.afjltd.tracking.room.AFJDatabase
import com.afjltd.tracking.room.model.TableUploadFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FileUploadRepo {


    companion object {

        var loginDatabase: AFJDatabase? = null

        var tableFileUploadModel: LiveData<TableUploadFile>? = null

        fun initializeDB(context: Context) : AFJDatabase {
            return AFJDatabase.getDataseClient(context)
        }

        fun insertData(context: Context, tableData: TableUploadFile) {

            loginDatabase = initializeDB(context)

            CoroutineScope(IO).launch {
                loginDatabase!!.loginDao().insertFileUpload(tableData)
            }

        }

        fun updateFileData(context: Context, tableData: TableUploadFile) {

            loginDatabase = initializeDB(context)

            CoroutineScope(IO).launch {
                loginDatabase!!.loginDao().updateFileInfo(tableData)
            }

        }

        fun getUploadFileDetails(context: Context, fileID: String) : LiveData<TableUploadFile>? {

            loginDatabase = initializeDB(context)

            tableFileUploadModel = loginDatabase!!.loginDao().getFileDetails(fileID)

            return tableFileUploadModel
        }


        fun getFileDetail(context: Context, fileID: String) :TableUploadFile{

            loginDatabase = initializeDB(context)

            val  data = loginDatabase!!.loginDao().getFileData(fileID)

            return data
        }

        fun getUncompletedFiles(context: Context) :List<TableUploadFile>{

            loginDatabase = initializeDB(context)

            val  data = loginDatabase!!.loginDao().getAllUploadFiles() //where satu

            return data
        }
        fun getErrorData(context: Context) :List<TableUploadFile>{

            loginDatabase = initializeDB(context)

            val  data = loginDatabase!!.loginDao().getAllErrorData() //where satu

            return data
        }
    }

}