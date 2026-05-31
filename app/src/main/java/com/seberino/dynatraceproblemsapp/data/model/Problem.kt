package com.seberino.dynatraceproblemsapp.data.model

data class ProblemsResponse(
    val totalCount: Int,
    val pageSize: Int,
    val problems: List<Problem>,
    val nextPageKey: String? = null
)

data class Problem(
    val displayId: String,
    val title: String,
    val startTime: Long,
    val status: String,
    val severityLevel: String,
    val affectedEntities: List<EntityInfo>
)

data class EntityInfo(
    val entityId: EntityIdInfo,
    val displayName: String?,
    val name: String?
)

data class EntityIdInfo(
    val id: String,
    val type: String?
)
