package com.seberino.dynatraceproblemsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance

@Database(entities = [DynatraceInstance::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instanceDao(): InstanceDao
}
