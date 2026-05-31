package com.seberino.dynatraceproblemsapp.data.model

data class ManagementZoneResponse(
    val values: List<ManagementZone>? = null,
    // For V2 settings fallback if needed, or just to match what we find
    val items: List<ManagementZoneItem>? = null
)

data class ManagementZone(
    val id: String,
    val name: String
)

data class ManagementZoneItem(
    val objectId: String,
    val summary: String
)
