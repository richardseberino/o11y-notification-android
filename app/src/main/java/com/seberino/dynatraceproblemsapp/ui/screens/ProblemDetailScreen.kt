package com.seberino.dynatraceproblemsapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seberino.dynatraceproblemsapp.data.model.Problem
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    problem: Problem,
    baseUrl: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val startDate = sdf.format(Date(problem.startTime))
    
    val endTime = if (problem.endTime != null && problem.endTime > 0) problem.endTime else System.currentTimeMillis()
    val diff = endTime - problem.startTime
    
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    
    val durationText = buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        append("${minutes}m")
    }.trim()
    
    val duration = if (problem.endTime == null || problem.endTime <= 0) {
        "$durationText (Aberto)"
    } else {
        durationText
    }

    val newDyna = baseUrl.replace("live", "apps")
    val dynatraceUrl = "${newDyna.trimEnd('/')}/ui/apps/dynatrace.davis.problems/problem/${problem.problemId}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Problem Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailItem(label = "ID", value = problem.displayId)
            DetailItem(label = "Title", value = problem.title)
            DetailItem(label = "Severity", value = problem.severityLevel)
            DetailItem(label = "Category / Impact", value = problem.impactLevel ?: "N/A")
            DetailItem(label = "Status", value = problem.status)
            DetailItem(label = "Started", value = startDate)
            DetailItem(label = "Duration", value = duration)

            if (problem.affectedEntities.isNotEmpty()) {
                Text(
                    text = "Affected Entities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                problem.affectedEntities.forEach { entity ->
                    Text(
                        text = "• ${entity.displayName ?: entity.name ?: entity.entityId.id}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Exibir Root Cause / Evidence se disponível
            problem.evidenceDetails?.details?.firstOrNull()?.let { evidence ->
                Text(
                    text = "Evidence / Root Cause",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Type: ${evidence.evidenceType}", style = MaterialTheme.typography.labelMedium)
                        Text(text = evidence.displayName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dynatraceUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open in Browser")
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Da uma olhada neste Problema aberto no Dynatrace: $dynatraceUrl")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}
