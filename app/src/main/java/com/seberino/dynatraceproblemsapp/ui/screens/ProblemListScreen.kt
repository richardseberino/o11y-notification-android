package com.seberino.dynatraceproblemsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seberino.dynatraceproblemsapp.data.model.Problem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemListScreen(
    problems: List<Problem>,
    isLoading: Boolean,
    totalCount: Int,
    nextPageKey: String?,
    onLoadMore: () -> Unit,
    onProblemClick: (Problem) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Problems")
                        Text(
                            text = "Showing ${problems.size} of $totalCount",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(problems) { problem ->
                    ProblemItem(problem = problem, onClick = { onProblemClick(problem) })
                }
                
                if (nextPageKey != null) {
                    item {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Button(
                                onClick = onLoadMore,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text("Load More")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProblemItem(problem: Problem, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val date = sdf.format(Date(problem.startTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = problem.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Started: $date", style = MaterialTheme.typography.bodySmall)
            if (problem.affectedEntities.isNotEmpty()) {
                Text(
                    text = "Affected: ${problem.affectedEntities.joinToString { it.displayName ?: it.name ?: it.entityId.id }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
