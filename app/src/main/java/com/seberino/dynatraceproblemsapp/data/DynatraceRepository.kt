package com.seberino.dynatraceproblemsapp.data

import com.seberino.dynatraceproblemsapp.data.local.InstanceDao
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import com.seberino.dynatraceproblemsapp.data.model.ProblemsResponse
import com.seberino.dynatraceproblemsapp.data.remote.DynatraceApiService
import kotlinx.coroutines.flow.Flow

class DynatraceRepository(
    private val instanceDao: InstanceDao,
    private val apiService: DynatraceApiService
) {
    val allInstances: Flow<List<DynatraceInstance>> = instanceDao.getAllInstances()

    suspend fun getInstanceById(id: Int) = instanceDao.getInstanceById(id)

    suspend fun insertInstance(instance: DynatraceInstance) = instanceDao.insertInstance(instance)

    suspend fun updateInstance(instance: DynatraceInstance) = instanceDao.updateInstance(instance)

    suspend fun deleteInstance(instance: DynatraceInstance) = instanceDao.deleteInstance(instance)

    suspend fun getManagementZones(url: String, token: String): Pair<List<com.seberino.dynatraceproblemsapp.data.model.ManagementZone>, String> {
        val baseUrl = url.trimEnd('/')
        // We will try the Config V1 endpoint as it is the most reliable for listing MZs with names and IDs
        val finalUrl = if (baseUrl.contains("/api/config/v1")) baseUrl 
                      else if (baseUrl.contains("/api")) "$baseUrl/config/v1/managementZones"
                      else "$baseUrl/api/config/v1/managementZones"
        
        return try {
            val response = apiService.getManagementZones(finalUrl, "Api-Token $token")
            val zones = response.values?.map { it } ?: emptyList()
            Pair(zones, "Success: Found ${zones.size} zones at $finalUrl")
        } catch (e: Exception) {
            val errorMsg = "Error at $finalUrl: ${e.localizedMessage}"
            android.util.Log.e("DynatraceRepo", errorMsg, e)
            Pair(emptyList(), errorMsg)
        }
    }

    suspend fun getProblemsForInstance(instance: DynatraceInstance, nextPageKey: String? = null): ProblemsResponse {
        val baseUrl = instance.url.trimEnd('/')
        val apiUrl = if (baseUrl.contains("/api/v2")) baseUrl else "$baseUrl/api/v2/problems"
        // Ensure we don't duplicate /problems if it's already there
        val finalUrl = if (apiUrl.endsWith("/problems")) apiUrl else "$apiUrl/problems"
        
        val token = "Api-Token ${instance.token}"
        
        val selector = instance.filterSegmentation?.takeIf { it.isNotBlank() }?.let {
            "status(\"open\"),$it"
        } ?: "status(\"open\")"

        return try {
            android.util.Log.d("DynatraceRepo", "Fetching problems from $finalUrl with selector: $selector, pageSize: ${instance.pageSize}, nextPageKey: $nextPageKey")
            val response = apiService.getProblems(
                url = finalUrl, 
                token = token, 
                problemSelector = selector, 
                pageSize = if (nextPageKey == null) instance.pageSize else null,
                nextPageKey = nextPageKey
            )
            android.util.Log.d("DynatraceRepo", "Found ${response.problems.size} problems, totalCount: ${response.totalCount}")
            response
        } catch (e: Exception) {
            android.util.Log.e("DynatraceRepo", "Error fetching problems: ${e.message}", e)
            com.seberino.dynatraceproblemsapp.data.model.ProblemsResponse(0, 0, emptyList())
        }
    }
}
