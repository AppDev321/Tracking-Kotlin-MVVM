package com.example.afjtracking.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.model.TableLocation
import com.example.afjtracking.room.model.TableUploadFile
import com.example.afjtracking.utils.Constants


@Dao
interface DAOAccess {
    //************ LOCATION TABLE ****************//

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationData(tableLocation: TableLocation)

    @Update
    fun updateLocationTable(tableLocation: TableLocation)

    @Query("SELECT * FROM tbl_location WHERE api_status = :status AND api_retry_count < :errorCount limit :apiDataLimit")
    fun getLocationTableData(status: Int =0 ,errorCount :Int = Constants.API_RETRY_COUNT ,apiDataLimit :Int = Constants.FILE_QUERY_LIMIT) : List<TableLocation>

    @Query("SELECT * FROM tbl_location WHERE   api_status =:status  AND api_retry_count >= :errorCount and error_posted_server = 0 limit :apiDataLimit")
    fun getAllErrorLocation(status: Int =0 , errorCount :Int = Constants.API_RETRY_COUNT , apiDataLimit :Int = Constants.FILE_QUERY_LIMIT) : List<TableLocation>





    //************ FILE UPLOAD TABLE ****************//

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(tableUploadFile : TableUploadFile)

    @Update
    fun updateFileInfo(tableUploadFile : TableUploadFile)

    @Query("SELECT * FROM upload_file WHERE upload_id =:fileId")
    fun getFileDetails(fileId: String?) : LiveData<TableUploadFile>

    @Query("SELECT * FROM upload_file WHERE upload_status = :status AND api_retry_count < :errorCount limit :apiDataLimit")
   fun getAllUploadFiles(status: Int =0 ,errorCount :Int = Constants.API_RETRY_COUNT ,apiDataLimit :Int = Constants.FILE_QUERY_LIMIT) : List<TableUploadFile>

    @Query("SELECT * FROM upload_file WHERE upload_status = :status AND api_retry_count >= :errorCount and error_posted_server = 0 limit :apiDataLimit")
    fun getAllErrorData(status: Int =0 ,errorCount :Int = Constants.API_RETRY_COUNT ,apiDataLimit :Int = Constants.FILE_QUERY_LIMIT) : List<TableUploadFile>

    @Query("SELECT * FROM upload_file WHERE upload_id =:fileId ")
    fun getFileData(fileId: String?) : TableUploadFile

    @Query("UPDATE upload_file SET file_path=:filePath , file_size=:size WHERE id = :id")
    fun updateFileRow(filePath: String?, size: String?,id :Int?)

    @Query("SELECT * FROM upload_file WHERE upload_id =:fileId and field_name = :fieldName")
    fun getFileData(fileId: String?, fieldName: String?) : TableUploadFile


    suspend fun insertFileUpload(tableUploadFile : TableUploadFile) {
        val itemsFromDB = getFileData(tableUploadFile.uploadID!!,tableUploadFile.fieldName)

        if (itemsFromDB != null  ) {
            tableUploadFile.Id = itemsFromDB.Id
            updateFileInfo(tableUploadFile)
        }
        else {
            insertData(tableUploadFile)
        }
    }




    //************ API TABLE ****************//


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertAPIData(tableApiData : TableAPIData)

    @Update
    fun updateAPIData(tableApiData : TableAPIData)

    @Query("SELECT * FROM api_result WHERE api_name =:apiName")
    fun getAPIData(apiName: String?) : LiveData<TableAPIData>

    @Query("SELECT * FROM api_result WHERE api_name =:apiName")
    fun getSingleRow(apiName: String?) : TableAPIData



    @Query("SELECT * FROM api_result WHERE   api_status =:status  AND api_retry_count < :errorCount limit :apiDataLimit")
    fun getAllAPIData(status: Int =0 , errorCount :Int = Constants.API_RETRY_COUNT , apiDataLimit :Int = Constants.FILE_QUERY_LIMIT) : List<TableAPIData>


    @Query("SELECT * FROM api_result WHERE   api_status =:status  AND api_retry_count >= :errorCount and error_posted_server = 0 limit :apiDataLimit")
    fun getAllErrorApiData(status: Int =0 , errorCount :Int = Constants.API_RETRY_COUNT , apiDataLimit :Int = Constants.FILE_QUERY_LIMIT) : List<TableAPIData>



    suspend fun insertApiRecordInDB(tableApiData : TableAPIData) {
        val itemsFromDB = getSingleRow(tableApiData.apiName)

        if (itemsFromDB != null  ) {
            tableApiData.Id = itemsFromDB.Id
            updateAPIData(tableApiData)
        }
        else {
            InsertAPIData(tableApiData)
        }
    }


}