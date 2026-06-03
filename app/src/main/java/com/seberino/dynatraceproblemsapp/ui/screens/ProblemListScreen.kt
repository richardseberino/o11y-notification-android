package com.seberino.dynatraceproblemsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onBack: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProblems = remember(problems, searchQuery) {
        if (searchQuery.isEmpty()) {
            problems
        } else {
            problems.filter { problem ->
                problem.title.contains(searchQuery, ignoreCase = true) ||
                problem.affectedEntities.any { entity ->
                    entity.displayName?.contains(searchQuery, ignoreCase = true) == true ||
                    entity.name?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Problems")
                        Text(
                            text = "Showing ${filteredProblems.size} of $totalCount",
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
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search by name or entity...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            if (isLoading && problems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredProblems) { problem ->
                        ProblemItem(problem = problem) { onProblemClick(problem) }
                    }
                    
                    if (nextPageKey != null && searchQuery.isEmpty()) {
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
}

@Composable
fun ProblemItem(problem: Problem, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val date = sdf.format(Date(problem.startTime))
    val (icon, color) = getSeverityIconAndColor(problem.severityLevel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = problem.severityLevel,
                tint = color,
                modifier = Modifier.size(32.dp).padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
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
}

@Composable
private fun getSeverityIconAndColor(severity: String): Pair<ImageVector, Color> {
    return when (severity.uppercase()) {
        "AVAILABILITY" -> Icons.Default.Error to Color(0xFFC62828) // Red
        "ERROR" -> Icons.Default.PriorityHigh to Color(0xFFE64A19) // Deep Orange
        "PERFORMANCE" -> Icons.Default.Bolt to Color(0xFFFBC02D) // Amber/Yellow
        "RESOURCE_CONTENTION" -> Icons.Default.Warning to Color(0xFFFFA000) // Orange
        "CUSTOM_ALERT" -> Icons.Default.Info to Color(0xFF1976D2) // Blue
        else -> Icons.Default.Info to Color.Gray
    }
}
