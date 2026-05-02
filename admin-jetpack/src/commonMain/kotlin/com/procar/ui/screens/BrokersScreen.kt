package com.procar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.createBroker
import com.procar.api.deleteBroker
import com.procar.api.fetchBrokers
import com.procar.api.updateBroker
import com.procar.gateway.api.dto.BrokerDto
import com.procar.gateway.api.dto.CreateBrokerRequest
import com.procar.gateway.api.dto.UpdateBrokerRequest
import com.procar.ui.components.DataTable
import com.procar.ui.state.launchWithState
import com.procar.ui.state.rememberScreenState
import kotlinx.coroutines.launch

private val BROKER_ID_REGEX = Regex("[a-z0-9\\-]+")

@Composable
fun BrokersScreen() {
    val scope = rememberCoroutineScope()
    val state = rememberScreenState()
    var brokers by remember { mutableStateOf<List<BrokerDto>>(emptyList()) }
    var selectedBroker by remember { mutableStateOf<BrokerDto?>(null) }

    fun load() {
        scope.launchWithState(state, "Error loading brokers") {
            brokers = fetchBrokers()
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { state.showCreateDialog = true }) { Text("Create Broker") }
            OutlinedButton(onClick = { load() }) { Text("Refresh") }
        }

        Spacer(Modifier.height(8.dp))
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DataTable(
                headers = listOf("ID", "Name", "Address", "Phones", "Emails", "Actions"),
                rows = brokers,
                rowKey = { it.id },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                cellContent = { b, col ->
                    when (col) {
                        0 -> Text(b.id)
                        1 -> Text(b.name)
                        2 -> Text(b.address)
                        3 -> Text(b.phones.joinToString(", "))
                        4 -> Text(b.emails.joinToString(", "))
                        5 -> TextButton(onClick = { selectedBroker = b }) { Text("Edit") }
                    }
                }
            )
        }
    }

    if (state.showCreateDialog) {
        CreateBrokerDialog(
            onDismiss = { state.showCreateDialog = false },
            onCreated = {
                state.showCreateDialog = false
                load()
            }
        )
    }

    selectedBroker?.let { broker ->
        EditBrokerDialog(
            broker = broker,
            onDismiss = { selectedBroker = null },
            onChanged = { updated ->
                brokers = brokers.map { if (it.id == updated.id) updated else it }
                selectedBroker = null
            },
            onDeleted = {
                brokers = brokers.filterNot { it.id == broker.id }
                selectedBroker = null
            }
        )
    }
}

private fun parseList(text: String): List<String> =
    text.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }

@Composable
private fun BrokerFormColumn(
    error: String?,
    name: String, onNameChange: (String) -> Unit, nameLabel: String = "Name",
    address: String, onAddressChange: (String) -> Unit,
    phones: String, onPhonesChange: (String) -> Unit,
    emails: String, onEmailsChange: (String) -> Unit,
    header: @Composable () -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(460.dp)
    ) {
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        header()
        OutlinedTextField(name, { onNameChange(it.replace("\t", "")) }, label = { Text(nameLabel) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(address, { onAddressChange(it.replace("\t", "")) }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(phones, { onPhonesChange(it.replace("\t", "")) }, label = { Text("Phones (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(emails, { onEmailsChange(it.replace("\t", "")) }, label = { Text("Emails (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@Composable
private fun CreateBrokerDialog(onDismiss: () -> Unit, onCreated: (BrokerDto) -> Unit) {
    val scope = rememberCoroutineScope()
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phones by remember { mutableStateOf("") }
    var emails by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Broker") },
        text = {
            BrokerFormColumn(
                error = error,
                name = name, onNameChange = { name = it }, nameLabel = "Name *",
                address = address, onAddressChange = { address = it },
                phones = phones, onPhonesChange = { phones = it },
                emails = emails, onEmailsChange = { emails = it },
            ) {
                OutlinedTextField(
                    id, { id = it.lowercase().replace("\t", "") },
                    label = { Text("ID (a-z 0-9 -) *") },
                    supportingText = { Text("Unique readable identifier, e.g. acme-brokers") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    if (id.isBlank() || !BROKER_ID_REGEX.matches(id)) {
                        error = "ID is required and must match [a-z0-9-]+"
                        return@Button
                    }
                    if (name.isBlank()) { error = "Name is required"; return@Button }
                    scope.launch {
                        saving = true
                        error = null
                        try {
                            val created = createBroker(CreateBrokerRequest(
                                id = id,
                                name = name,
                                address = address,
                                phones = parseList(phones),
                                emails = parseList(emails),
                            ))
                            onCreated(created)
                        } catch (e: Exception) {
                            error = "Error creating broker: ${e.message}"
                        } finally {
                            saving = false
                        }
                    }
                }
            ) {
                if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Create")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditBrokerDialog(
    broker: BrokerDto,
    onDismiss: () -> Unit,
    onChanged: (BrokerDto) -> Unit,
    onDeleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember(broker.id) { mutableStateOf(broker.name) }
    var address by remember(broker.id) { mutableStateOf(broker.address) }
    var phones by remember(broker.id) { mutableStateOf(broker.phones.joinToString(", ")) }
    var emails by remember(broker.id) { mutableStateOf(broker.emails.joinToString(", ")) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broker: ${broker.id}") },
        text = {
            BrokerFormColumn(
                error = error,
                name = name, onNameChange = { name = it },
                address = address, onAddressChange = { address = it },
                phones = phones, onPhonesChange = { phones = it },
                emails = emails, onEmailsChange = { emails = it },
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !working,
                    onClick = {
                        scope.launch {
                            working = true
                            error = null
                            try {
                                val updated = updateBroker(broker.id, UpdateBrokerRequest(
                                    name = name,
                                    address = address,
                                    phones = parseList(phones),
                                    emails = parseList(emails),
                                ))
                                onChanged(updated)
                            } catch (e: Exception) {
                                error = "Error saving: ${e.message}"
                            } finally {
                                working = false
                            }
                        }
                    }
                ) { Text("Save") }
                OutlinedButton(
                    enabled = !working,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        scope.launch {
                            working = true
                            error = null
                            try {
                                deleteBroker(broker.id)
                                onDeleted()
                            } catch (e: Exception) {
                                error = "Error deleting: ${e.message}"
                            } finally {
                                working = false
                            }
                        }
                    }
                ) { Text("Delete") }
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Close") } }
    )
}
