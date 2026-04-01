package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.createWarehouse
import com.procar.api.fetchWarehouses
import com.procar.model.AdminCreateWarehouseRequest
import com.procar.model.AdminWarehouseResponse
import kotlinx.coroutines.launch

@Composable
fun WarehousesScreen() {
    val scope = rememberCoroutineScope()
    var warehouses by remember { mutableStateOf<List<AdminWarehouseResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf<AdminWarehouseResponse?>(null) }

    fun loadWarehouses() {
        scope.launch {
            loading = true
            errorMessage = null
            try {
                warehouses = fetchWarehouses()
            } catch (e: Exception) {
                errorMessage = "Error loading warehouses: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadWarehouses() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Warehouses", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showCreateDialog = true }) { Text("Create Warehouse") }
            OutlinedButton(onClick = { loadWarehouses() }) { Text("Refresh") }
        }

        Spacer(Modifier.height(8.dp))
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DataTable(
                headers = listOf("ID", "Name", "City", "State", "Country", "Timezone", "Contact", "Actions"),
                rows = warehouses,
                rowKey = { it.id },
                cellContent = { wh, col ->
                    when (col) {
                        0 -> Text(wh.id.take(8) + "…", style = MaterialTheme.typography.bodySmall)
                        1 -> Text(wh.name)
                        2 -> Text(wh.city)
                        3 -> Text(wh.state)
                        4 -> Text(wh.country)
                        5 -> Text(wh.timezone)
                        6 -> Text(wh.contactName ?: "—")
                        7 -> TextButton(onClick = { selectedWarehouse = wh }) { Text("Options") }
                    }
                }
            )
        }
    }

    if (showCreateDialog) {
        CreateWarehouseDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = {
                showCreateDialog = false
                loadWarehouses()
            }
        )
    }

    selectedWarehouse?.let { warehouse ->
        WarehouseOptionsDialog(
            warehouse = warehouse,
            onDismiss = { selectedWarehouse = null }
        )
    }
}

@Composable
fun CreateWarehouseDialog(onDismiss: () -> Unit, onCreated: () -> Unit) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("UTC") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }

    fun validate(): String? {
        if (name.isBlank()) return "Name is required"
        if (address.isBlank()) return "Address is required"
        if (city.isBlank()) return "City is required"
        if (state.isBlank()) return "State is required"
        if (zipCode.isBlank()) return "ZIP code is required"
        if (country.isBlank()) return "Country is required"
        if (timezone.isBlank()) return "Timezone is required"
        return null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Warehouse") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(460.dp)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(name, { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(city, { city = it }, label = { Text("City *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(state, { state = it }, label = { Text("State *") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(zipCode, { zipCode = it }, label = { Text("ZIP *") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(country, { country = it }, label = { Text("Country *") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(timezone, { timezone = it }, label = { Text("Timezone *") }, modifier = Modifier.fillMaxWidth())
                SectionHeader("Contact (optional)")
                OutlinedTextField(contactName, { contactName = it }, label = { Text("Contact Name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(contactPhone, { contactPhone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(contactEmail, { contactEmail = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    val validationError = validate()
                    if (validationError != null) { error = validationError; return@Button }
                    scope.launch {
                        saving = true
                        try {
                            createWarehouse(AdminCreateWarehouseRequest(
                                name = name, address = address, city = city,
                                state = state, zipCode = zipCode, country = country,
                                timezone = timezone,
                                contactName = contactName.takeIf { it.isNotBlank() },
                                contactPhone = contactPhone.takeIf { it.isNotBlank() },
                                contactEmail = contactEmail.takeIf { it.isNotBlank() },
                            ))
                            onCreated()
                        } catch (e: Exception) {
                            error = "Error creating warehouse: ${e.message}"
                        } finally {
                            saving = false
                        }
                    }
                }
            ) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Save")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WarehouseOptionsDialog(warehouse: AdminWarehouseResponse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Warehouse Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ID: ${warehouse.id}", style = MaterialTheme.typography.bodySmall)
                Text("Name: ${warehouse.name}")
                Text("Address: ${warehouse.address}")
                Text("Location: ${warehouse.city}, ${warehouse.state} ${warehouse.zipCode}")
                Text("Country: ${warehouse.country}")
                Text("Timezone: ${warehouse.timezone}")
                warehouse.contactName?.let { Text("Contact: $it") }
                warehouse.contactPhone?.let { Text("Phone: $it") }
                warehouse.contactEmail?.let { Text("Email: $it") }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete Warehouse") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
