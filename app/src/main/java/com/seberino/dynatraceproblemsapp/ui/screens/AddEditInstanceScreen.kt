package com.seberino.dynatraceproblemsapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seberino.dynatraceproblemsapp.data.model.DynatraceInstance
import com.seberino.dynatraceproblemsapp.data.model.ManagementZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInstanceScreen(
    instance: DynatraceInstance? = null,
    managementZones: List<ManagementZone>,
    debugMessage: String = "",
    onFetchZones: (String, String) -> Unit,
    onSave: (String, String, String, String?, Int, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(instance?.name ?: "") }
    var url by remember { mutableStateOf(instance?.url ?: "") }
    var token by remember { mutableStateOf(instance?.token ?: "") }
    var filter by remember { mutableStateOf(instance?.filterSegmentation ?: "") }
    var pageSize by remember { mutableStateOf(instance?.pageSize?.toString() ?: "10") }
    var notificationsEnabled by remember { mutableStateOf(instance?.notificationsEnabled ?: false) }
    
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (instance == null) "Add Instance" else "Edit Instance") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Instance Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("API Token") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = pageSize,
                onValueChange = { if (it.all { char -> char.isDigit() }) pageSize = it },
                label = { Text("Items per page") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onFetchZones(url, token) },
                    enabled = url.isNotBlank() && token.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Fetch Zones")
                }
            }

            if (debugMessage.isNotBlank()) {
                Text(
                    text = debugMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (debugMessage.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    label = { Text("Segmentation Filter") },
                    placeholder = { Text("Select MZ or type custom") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nenhum (Todos os problemas abertos)") },
                        onClick = {
                            filter = ""
                            expanded = false
                        }
                    )
                    managementZones.forEach { mz ->
                        DropdownMenuItem(
                            text = { Text(mz.name) },
                            onClick = {
                                filter = "managementZoneIds(\"${mz.id}\")"
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Enable Push Notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(name, url, token, filter.ifBlank { null }, pageSize.toIntOrNull() ?: 10, notificationsEnabled) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && url.isNotBlank() && token.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
