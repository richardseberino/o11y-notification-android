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

    private fun getApiUrl(baseUrl: String, endpoint: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        return when {
            cleanBase.endsWith(endpoint) -> cleanBase
            cleanBase.contains("/api/v2") -> "$cleanBase/${endpoint.removePrefix("/api/v2/").removePrefix("/")}"
            else -> "$cleanBase/api/v2/${endpoint.removePrefix("/")}"
        }
    }

    suspend fun getProblemsForInstance(instance: DynatraceInstance, nextPageKey: String? = null): ProblemsResponse {
        val finalUrl = getApiUrl(instance.url, "problems")
        val token = "Api-Token ${instance.token}"
        
        // Se temos nextPageKey, os filtros já estão embutidos nele e 'from' deve ser null
        val selector = if (nextPageKey == null) {
            instance.filterSegmentation?.takeIf { it.isNotBlank() }?.let {
                "status(\"open\"),$it"
            } ?: "status(\"open\")"
        } else null

        val fromTime = if (nextPageKey == null) "now-30d" else null

        return try {
            android.util.Log.d("DynatraceRepo", "Fetching problems. nextPageKey: ${nextPageKey != null}")
            val response = apiService.getProblems(
                url = finalUrl, 
                token = token, 
                problemSelector = selector, 
                from = fromTime,
                pageSize = if (nextPageKey == null) instance.pageSize else null,
                nextPageKey = nextPageKey
            )
            android.util.Log.d("DynatraceRepo", "Received ${response.problems.size} problems. NextPageKey exists: ${response.nextPageKey != null}")
            response
        } catch (e: Exception) {
            android.util.Log.e("DynatraceRepo", "Error fetching problems: ${e.message}", e)
            ProblemsResponse(0, 0, emptyList())
        }
    }

    suspend fun getProblemWithDetails(instance: DynatraceInstance, problemId: String): com.seberino.dynatraceproblemsapp.data.model.Problem? {
        val finalUrl = getApiUrl(instance.url, "problems/$problemId")
        return try {
            apiService.getProblemDetails(finalUrl, "Api-Token ${instance.token}")
        } catch (e: Exception) {
            android.util.Log.e("DynatraceRepo", "Error fetching problem details: ${e.message}")
            null
        }
    }
}
