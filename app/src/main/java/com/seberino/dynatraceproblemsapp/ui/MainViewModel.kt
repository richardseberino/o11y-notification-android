package com.seberino.dynatraceproblemsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seberino.dynatraceproblemsapp.data.DynatraceRepository
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import com.seberino.dynatraceproblemsapp.data.model.ManagementZone
import com.seberino.dynatraceproblemsapp.data.model.Problem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DynatraceRepository) : ViewModel() {

    val instances = repository.allInstances

    private val _problems = MutableStateFlow<List<Problem>>(emptyList())
    val problems: StateFlow<List<Problem>> = _problems.asStateFlow()

    private val _nextPageKey = MutableStateFlow<String?>(null)
    val nextPageKey: StateFlow<String?> = _nextPageKey.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _problemCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val problemCounts: StateFlow<Map<Int, Int>> = _problemCounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _managementZones = MutableStateFlow<List<ManagementZone>>(emptyList())
    val managementZones: StateFlow<List<ManagementZone>> = _managementZones.asStateFlow()

    private val _debugMessage = MutableStateFlow("")
    val debugMessage: StateFlow<String> = _debugMessage.asStateFlow()

    private val _problemDetail = MutableStateFlow<Problem?>(null)
    val problemDetail: StateFlow<Problem?> = _problemDetail.asStateFlow()

    init {
        viewModelScope.launch {
            instances.collect { list ->
                refreshProblemCounts(list)
            }
        }
    }

    fun loadProblemDetails(instanceId: Int, displayId: String) {
        viewModelScope.launch {
            _problemDetail.value = null
            
            val instance = repository.getInstanceById(instanceId)
            if (instance != null) {
                val basicProblem = _problems.value.find { it.displayId == displayId }
                if (basicProblem != null) {
                    val detailed = repository.getProblemWithDetails(instance, basicProblem.problemId)
                    _problemDetail.value = detailed
                }
            }
        }
    }

    fun loadProblems(instanceId: Int, loadNextPage: Boolean = false) {
        viewModelScope.launch {
            if (loadNextPage && _nextPageKey.value == null) return@launch
            
            _isLoading.value = true
            
            if (!loadNextPage) {
                _problems.value = emptyList()
                _nextPageKey.value = null
            }

            val instance = repository.getInstanceById(instanceId)
            if (instance != null) {
                val response = repository.getProblemsForInstance(instance, _nextPageKey.value)
                if (loadNextPage) {
                    _problems.value = _problems.value + response.problems
                } else {
                    _problems.value = response.problems
                }
                _totalCount.value = response.totalCount
                _nextPageKey.value = response.nextPageKey
            }
            _isLoading.value = false
        }
    }

    fun fetchManagementZones(url: String, token: String) {
        viewModelScope.launch {
            _debugMessage.value = "Fetching..."
            val result = repository.getManagementZones(url, token)
            _managementZones.value = result.first
            _debugMessage.value = result.second
        }
    }

    fun refreshProblemCounts(instances: List<DynatraceInstance>) {
        viewModelScope.launch {
            val counts = _problemCounts.value.toMutableMap()
            instances.forEach { instance ->
                val response = repository.getProblemsForInstance(instance)
                counts[instance.id] = response.totalCount
            }
            _problemCounts.value = counts
        }
    }

    fun getProblemById(displayId: String): Problem? {
        return _problems.value.find { it.displayId == displayId }
    }

    fun addInstance(name: String, url: String, token: String, filter: String?, pageSize: Int, notificationsEnabled: Boolean) {
        viewModelScope.launch {
            repository.insertInstance(DynatraceInstance(
                name = name, 
                url = url, 
                token = token, 
                filterSegmentation = filter, 
                pageSize = pageSize,
                notificationsEnabled = notificationsEnabled
            ))
        }
    }

    fun updateInstance(id: Int, name: String, url: String, token: String, filter: String?, pageSize: Int, notificationsEnabled: Boolean) {
        viewModelScope.launch {
            val existing = repository.getInstanceById(id)
            repository.updateInstance(DynatraceInstance(
                id = id, 
                name = name, 
                url = url, 
                token = token, 
                filterSegmentation = filter, 
                pageSize = pageSize,
                notificationsEnabled = notificationsEnabled,
                lastSeenProblemId = existing?.lastSeenProblemId
            ))
        }
    }

    suspend fun getInstanceById(id: Int) = repository.getInstanceById(id)

    fun deleteInstance(instance: DynatraceInstance) {
        viewModelScope.launch {
            repository.deleteInstance(instance)
        }
    }
}
