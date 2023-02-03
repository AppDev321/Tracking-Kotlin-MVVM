package com.afjltd.tracking.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.afjltd.tracking.room.model.TableAPIData
import com.afjltd.tracking.room.model.TableLocation
import com.afjltd.tracking.room.model.TableUploadFile


@Database(entities = arrayOf(TableUploadFile::class, TableAPIData::class,TableLocation::class), version = 1, exportSchema = false)

abstract class AFJDatabase : RoomDatabase() {

    abstract fun loginDao() : DAOAccess

    companion object {

        @Volatile  //this will tell variable value to all instance in application
        private var INSTANCE: AFJDatabase? = null

        fun getDataseClient(context: Context) : AFJDatabase {

            if (INSTANCE != null) return INSTANCE!!

            synchronized(this) { //this not create muptlipe thread and lock this instance

                INSTANCE = Room
                    .databaseBuilder(context, AFJDatabase::class.java, "AJF_DATABASE")
                    .fallbackToDestructiveMigration()
                   
                    .build()

                return INSTANCE!!

            }
        }

    }

}
