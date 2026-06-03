package com.seberino.dynatraceproblemsapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seberino.dynatraceproblemsapp.BuildConfig
import com.seberino.dynatraceproblemsapp.R
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceListScreen(
    instances: List<DynatraceInstance>,
    problemCounts: Map<Int, Int>,
    onInstanceClick: (Int) -> Unit,
    onAddInstanceClick: () -> Unit,
    onEditInstanceClick: (Int) -> Unit,
    onDeleteInstance: (DynatraceInstance) -> Unit,
    onRefresh: () -> Unit
) {
    val currentDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    val version = BuildConfig.VERSION_NAME

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).padding(end = 8.dp)
                        )
                        Text("Dynatrace Instances")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Versão $version - $currentDate",
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddInstanceClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Instance")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Refresh All")
            }
            
            LazyColumn {
                items(instances) { instance ->
                    InstanceItem(
                        instance = instance,
                        problemCount = problemCounts[instance.id] ?: 0,
                        onClick = { onInstanceClick(instance.id) },
                        onEdit = { onEditInstanceClick(instance.id) },
                        onDelete = { onDeleteInstance(instance) }
                    )
                }
            }
        }
    }
}

@Composable
fun InstanceItem(
    instance: DynatraceInstance,
    problemCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = instance.name, style = MaterialTheme.typography.titleLarge)
                Text(text = instance.url, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Problems: $problemCount",
                    color = if (problemCount > 0) Color.Red else Color.Green,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
