package com.seberino.dynatraceproblemsapp.data.local

import androidx.room.*
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {
    @Query("SELECT * FROM dynatrace_instances")
    fun getAllInstances(): Flow<List<DynatraceInstance>>

    @Query("SELECT * FROM dynatrace_instances WHERE id = :id")
    suspend fun getInstanceById(id: Int): DynatraceInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: DynatraceInstance): Unit

    @Update
    suspend fun updateInstance(instance: DynatraceInstance): Unit

    @Delete
    suspend fun deleteInstance(instance: DynatraceInstance): Unit
}
