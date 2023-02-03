package com.afjltd.tracking.room.repository

import android.content.Context
import com.afjltd.tracking.room.AFJDatabase
import com.afjltd.tracking.room.model.TableAPIData
import com.afjltd.tracking.room.model.TableLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class LocationTableRepo {


    companion object {

        var afjDatabase: AFJDatabase? = null



        fun initializeDB(context: Context) : AFJDatabase {
            return AFJDatabase.getDataseClient(context)
        }

        fun insertLocationData(context: Context, tableData: TableLocation) {

            afjDatabase = initializeDB(context)

            CoroutineScope(IO).launch {
                afjDatabase!!.loginDao().insertLocationData(tableData)
            }

        }

        fun updateLocationData(context: Context, tableData: TableLocation) {
            afjDatabase = initializeDB(context)
            CoroutineScope(IO).launch {
                afjDatabase!!.loginDao().updateLocationTable(tableData)
            }
        }





        fun getSingleLocation(context: Context, apiName: String) :TableAPIData{
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getSingleRow(apiName)
            return data
        }


        fun getUnCompletedLocationRequest(context: Context) :List<TableLocation>
        {
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getLocationTableData() //where satuts
            return data
        }

        fun getErrorResultsData(context: Context) :List<TableLocation>
        {
            afjDatabase = initializeDB(context)
            val  data = afjDatabase!!.loginDao().getAllErrorLocation() //where satuts
            return data
        }





    }

}