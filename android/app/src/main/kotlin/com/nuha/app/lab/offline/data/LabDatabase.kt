package com.nuha.app.lab.offline.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Local database for the offline experiment only. The real app data layer will be designed separately. */
@Database(entities = [SessionResult::class, RemoteResult::class], version = 1, exportSchema = false)
abstract class LabDatabase : RoomDatabase() {
    abstract fun results(): ResultDao

    companion object {
        @Volatile private var instance: LabDatabase? = null

        fun get(context: Context): LabDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, LabDatabase::class.java, "nuha-lab.db")
                .build()
                .also { instance = it }
        }
    }
}
