package com.afjltd.tracking.room.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.afjltd.tracking.room.AFJDatabase
import com.afjltd.tracking.room.model.TableAPIData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ApiDataRepo {


    companion object {

        var afjDatabase: AFJDatabase? = null



        fun initializeDB(context: Context) : AFJDatabase {
            return AFJDatabase.getDataseClient(context)
        }

        fun insertData(context: Context, tableData: TableAPIData) {

            afjDatabase = initializeDB(context)

            CoroutineScope(IO).launch {
                afjDatabase!!.loginDao().insertApiRecordInDB(tableData)
            }

        }

        fun updateApiData(context: Context, tableData: TableAPIData) {
            afjDatabase = initializeDB(context)
            CoroutineScope(IO).launch {
                afjDatabase!!.loginDao().updateAPIData(tableData)
            }
        }

        fun getApiDataDetail(context: Context, apiName: String) :LiveData<TableAPIData>{
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getAPIData(apiName)
            return data
        }


        fun getApiSingleData(context: Context, apiName: String) :LiveData<TableAPIData>{
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getAPIData(apiName)
            return data
        }


        fun getApiData(context: Context, apiName: String) :TableAPIData{
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getSingleRow(apiName)
            return data
        }


        fun getUnCompletedRequest(context: Context) :List<TableAPIData>
        {
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getAllAPIData() //where satuts
            return data
        }
        fun getErrorResultsData(context: Context) :List<TableAPIData>
        {
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getAllErrorApiData() //where satuts
            return data
        }





    }

}