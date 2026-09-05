package com.safesphere.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.safesphere.data.local.dao.ContactDao
import com.safesphere.data.local.dao.IncidentLogDao
import com.safesphere.data.local.entity.EmergencyContactEntity
import com.safesphere.data.local.entity.IncidentLogEntity

@Database(
    entities = [
        EmergencyContactEntity::class,
        IncidentLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun incidentLogDao(): IncidentLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safesphere_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
