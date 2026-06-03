package com.seberino.dynatraceproblemsapp.data.model

data class ProblemsResponse(
    val totalCount: Int,
    val pageSize: Int,
    val problems: List<Problem>,
    val nextPageKey: String? = null
)

data class Problem(
    val problemId: String,
    val displayId: String,
    val title: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String,
    val severityLevel: String,
    val impactLevel: String? = null,
    val affectedEntities: List<EntityInfo>,
    val evidenceDetails: EvidenceDetails? = null
)

data class EvidenceDetails(
    val totalCount: Int,
    val details: List<Evidence>
)

data class Evidence(
    val evidenceType: String,
    val displayName: String,
    val entity: EntityInfo? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val dataPoints: List<DataPoint>? = null
)

data class DataPoint(
    val timestamp: Long,
    val value: Double
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
