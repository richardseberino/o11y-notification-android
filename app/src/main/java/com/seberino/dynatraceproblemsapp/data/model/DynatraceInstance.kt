package com.seberino.dynatraceproblemsapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dynatrace_instances")
data class DynatraceInstance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val token: String,
    val filterSegmentation: String? = null,
    val pageSize: Int = 10,
    val lastSeenProblemId: String? = null,
    val notificationsEnabled: Boolean = false
)
