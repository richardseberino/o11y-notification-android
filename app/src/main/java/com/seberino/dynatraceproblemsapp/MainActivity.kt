package com.seberino.dynatraceproblemsapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.seberino.dynatraceproblemsapp.data.Graph
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import com.seberino.dynatraceproblemsapp.ui.MainViewModel
import com.seberino.dynatraceproblemsapp.ui.screens.AddEditInstanceScreen
import com.seberino.dynatraceproblemsapp.ui.screens.InstanceListScreen
import com.seberino.dynatraceproblemsapp.ui.screens.ProblemDetailScreen
import com.seberino.dynatraceproblemsapp.ui.screens.ProblemListScreen
import com.seberino.dynatraceproblemsapp.ui.theme.DynatraceProblemsAppTheme
import com.seberino.dynatraceproblemsapp.worker.ProblemWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupBackgroundWork()
        setContent {
            DynatraceProblemsAppTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                
                // Request notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { }
                    LaunchedEffect(Unit) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val viewModel: MainViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MainViewModel(Graph.repository) as T
                        }
                    }
                )

                NavHost(navController = navController, startDestination = "instances") {
                    composable("instances") {
                        val instances by viewModel.instances.collectAsState(initial = emptyList())
                        val problemCounts by viewModel.problemCounts.collectAsState()
                        
                        InstanceListScreen(
                            instances = instances,
                            problemCounts = problemCounts,
                            onInstanceClick = { id ->
                                navController.navigate("problems/$id")
                            },
                            onAddInstanceClick = {
                                navController.navigate("add_instance")
                            },
                            onEditInstanceClick = { id ->
                                navController.navigate("edit_instance/$id")
                            },
                            onDeleteInstance = { viewModel.deleteInstance(it) },
                            onRefresh = { viewModel.refreshProblemCounts(instances) }
                        )
                    }
                    composable("add_instance") {
                        val managementZones by viewModel.managementZones.collectAsState()
                        val debugMessage by viewModel.debugMessage.collectAsState()
                        AddEditInstanceScreen(
                            managementZones = managementZones,
                            debugMessage = debugMessage,
                            onFetchZones = { url, token -> viewModel.fetchManagementZones(url, token) },
                            onSave = { name, url, token, filter, pageSize, notificationsEnabled ->
                                viewModel.addInstance(name, url, token, filter, pageSize, notificationsEnabled)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "edit_instance/{instanceId}",
                        arguments = listOf(navArgument("instanceId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val instanceId = backStackEntry.arguments?.getInt("instanceId") ?: 0
                        var instance by remember { mutableStateOf<DynatraceInstance?>(null) }
                        val managementZones by viewModel.managementZones.collectAsState()
                        val debugMessage by viewModel.debugMessage.collectAsState()
                        
                        LaunchedEffect(instanceId) {
                            instance = viewModel.getInstanceById(instanceId)
                            instance?.let {
                                viewModel.fetchManagementZones(it.url, it.token)
                            }
                        }

                        if (instance != null) {
                            AddEditInstanceScreen(
                                instance = instance,
                                managementZones = managementZones,
                                debugMessage = debugMessage,
                                onFetchZones = { url, token -> viewModel.fetchManagementZones(url, token) },
                                onSave = { name, url, token, filter, pageSize, notificationsEnabled ->
                                    viewModel.updateInstance(instanceId, name, url, token, filter, pageSize, notificationsEnabled)
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(
                        "problems/{instanceId}",
                        arguments = listOf(navArgument("instanceId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val instanceId = backStackEntry.arguments?.getInt("instanceId") ?: 0
                        val problems by viewModel.problems.collectAsState()
                        val isLoading by viewModel.isLoading.collectAsState()
                        val totalCount by viewModel.totalCount.collectAsState()
                        val nextPageKey by viewModel.nextPageKey.collectAsState()

                        LaunchedEffect(instanceId) {
                            viewModel.loadProblems(instanceId)
                        }

                        ProblemListScreen(
                            problems = problems,
                            isLoading = isLoading,
                            totalCount = totalCount,
                            nextPageKey = nextPageKey,
                            onLoadMore = { viewModel.loadProblems(instanceId, loadNextPage = true) },
                            onProblemClick = { problem ->
                                navController.navigate("problem_detail/${instanceId}/${problem.displayId}")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "problem_detail/{instanceId}/{displayId}",
                        arguments = listOf(
                            navArgument("instanceId") { type = NavType.IntType },
                            navArgument("displayId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val instanceId = backStackEntry.arguments?.getInt("instanceId") ?: 0
                        val displayId = backStackEntry.arguments?.getString("displayId") ?: ""
                        val problem by viewModel.problemDetail.collectAsState()
                        var instance by remember { mutableStateOf<DynatraceInstance?>(null) }
                        
                        LaunchedEffect(instanceId, displayId) {
                            instance = Graph.repository.getInstanceById(instanceId)
                            viewModel.loadProblemDetails(instanceId, displayId)
                        }

                        if (problem != null && instance != null) {
                            ProblemDetailScreen(
                                problem = problem!!,
                                baseUrl = instance!!.url,
                                onBack = { navController.popBackStack() }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupBackgroundWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ProblemWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ProblemCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
