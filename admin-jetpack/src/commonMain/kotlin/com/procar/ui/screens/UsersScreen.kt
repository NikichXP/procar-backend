package com.procar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.blockUser
import com.procar.api.createUser
import com.procar.api.fetchBrokers
import com.procar.api.fetchUsers
import com.procar.api.updateUserBroker
import com.procar.api.updateUserRoles
import com.procar.model.BrokerDto
import com.procar.model.CreateUserRequest
import com.procar.model.UserDto
import com.procar.model.UserRole
import com.procar.ui.components.DataTable
import com.procar.ui.components.EnumDropdown
import com.procar.ui.components.SectionHeader
import com.procar.ui.state.launchWithState
import com.procar.ui.state.rememberScreenState
import kotlinx.coroutines.launch

@Composable
fun UsersScreen() {
    val scope = rememberCoroutineScope()
    val state = rememberScreenState()
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var brokers by remember { mutableStateOf<List<BrokerDto>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<UserDto?>(null) }

    fun load() {
        scope.launchWithState(state, "Error loading users") {
            users = fetchUsers()
            brokers = fetchBrokers()
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Users", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { state.showCreateDialog = true }) { Text("Create User") }
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
                headers = listOf("ID", "Username", "Roles", "Broker", "Status", "Actions"),
                rows = users,
                rowKey = { it.id },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                cellContent = { user, col ->
                    when (col) {
                        0 -> Text(user.id.take(8) + "…", style = MaterialTheme.typography.bodySmall)
                        1 -> Text(user.username)
                        2 -> Text(user.roles.joinToString(", ") { it.name })
                        3 -> Text(user.brokerOrgId ?: "—")
                        4 -> BlockedChip(user.blocked)
                        5 -> TextButton(onClick = { selectedUser = user }) { Text("Manage") }
                    }
                }
            )
        }
    }

    if (state.showCreateDialog) {
        CreateUserDialog(
            brokers = brokers,
            onDismiss = { state.showCreateDialog = false },
            onCreated = {
                state.showCreateDialog = false
                load()
            }
        )
    }

    selectedUser?.let { user ->
        UserOptionsDialog(
            user = user,
            brokers = brokers,
            onDismiss = { selectedUser = null },
            onChanged = { updated ->
                users = users.map { if (it.id == updated.id) updated else it }
                selectedUser = updated
            }
        )
    }
}

@Composable
private fun BlockedChip(blocked: Boolean) {
    val color = if (blocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val label = if (blocked) "BLOCKED" else "ACTIVE"
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun UserOptionsDialog(
    user: UserDto,
    brokers: List<BrokerDto>,
    onDismiss: () -> Unit,
    onChanged: (UserDto) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Local role selection state
    val roleState = remember(user.id) {
        mutableStateMapOf<UserRole, Boolean>().apply {
            UserRole.entries.forEach { put(it, user.roles.contains(it)) }
        }
    }
    var brokerId by remember(user.id) { mutableStateOf(user.brokerOrgId ?: "") }

    fun runAsync(block: suspend () -> Unit) {
        scope.launch {
            working = true
            error = null
            try { block() } catch (e: Exception) { error = e.message } finally { working = false }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User: ${user.username}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.width(440.dp)) {
                Text("ID: ${user.id}", style = MaterialTheme.typography.bodySmall)
                Text("Status: ${if (user.blocked) "BLOCKED" else "ACTIVE"}")

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                SectionHeader("Roles")
                UserRole.entries.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = roleState[role] == true,
                            onCheckedChange = { roleState[role] = it }
                        )
                        Text(role.name)
                    }
                }
                Button(
                    enabled = !working,
                    onClick = {
                        runAsync {
                            val roles = UserRole.entries.filter { roleState[it] == true }
                            val updated = updateUserRoles(user.id, roles)
                            onChanged(updated)
                        }
                    }
                ) { Text("Save Roles") }

                SectionHeader("Broker")
                BrokerPicker(
                    brokers = brokers,
                    selectedId = brokerId.ifBlank { null },
                    onSelected = { brokerId = it ?: "" }
                )
                OutlinedTextField(
                    value = brokerId,
                    onValueChange = { brokerId = it },
                    label = { Text("Broker id (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !working,
                        onClick = {
                            runAsync {
                                val updated = updateUserBroker(user.id, brokerId.takeIf { it.isNotBlank() })
                                onChanged(updated)
                            }
                        }
                    ) { Text("Save Broker") }
                    OutlinedButton(
                        enabled = !working,
                        onClick = {
                            runAsync {
                                brokerId = ""
                                val updated = updateUserBroker(user.id, null)
                                onChanged(updated)
                            }
                        }
                    ) { Text("Unassign") }
                }

                SectionHeader("Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !working && !user.blocked,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            runAsync { onChanged(blockUser(user.id, true)) }
                        }
                    ) { Text("Block") }
                    OutlinedButton(
                        enabled = !working && user.blocked,
                        onClick = {
                            runAsync { onChanged(blockUser(user.id, false)) }
                        }
                    ) { Text("Unblock") }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {}
    )
}

@Composable
private fun BrokerPicker(
    brokers: List<BrokerDto>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
) {
    val options = listOf("— none —") + brokers.map { "${it.id} — ${it.name}" }
    val current = selectedId?.let { id -> brokers.firstOrNull { it.id == id }?.let { "${it.id} — ${it.name}" } ?: id }
        ?: "— none —"
    EnumDropdown(
        label = "Assign broker",
        selected = current,
        options = options,
        onSelected = { chosen ->
            if (chosen == "— none —") onSelected(null)
            else onSelected(chosen.substringBefore(" — "))
        }
    )
}

@Composable
private fun CreateUserDialog(
    brokers: List<BrokerDto>,
    onDismiss: () -> Unit,
    onCreated: (UserDto) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var brokerId by remember { mutableStateOf("") }
    val roleState = remember {
        mutableStateMapOf<UserRole, Boolean>().apply {
            put(UserRole.USER, true)
            put(UserRole.BROKER, false)
            put(UserRole.ADMIN, false)
        }
    }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(420.dp)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(
                    username, { username = it },
                    label = { Text("Username *") },
                    modifier = Modifier.fillMaxWidth()
                )

                SectionHeader("Roles")
                UserRole.entries.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = roleState[role] == true,
                            onCheckedChange = { roleState[role] = it }
                        )
                        Text(role.name)
                    }
                }

                SectionHeader("Broker (optional)")
                BrokerPicker(
                    brokers = brokers,
                    selectedId = brokerId.ifBlank { null },
                    onSelected = { brokerId = it ?: "" }
                )
                OutlinedTextField(
                    value = brokerId,
                    onValueChange = { brokerId = it },
                    label = { Text("Broker id") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    if (username.isBlank()) { error = "Username is required"; return@Button }
                    scope.launch {
                        saving = true
                        error = null
                        try {
                            val roles = UserRole.entries.filter { roleState[it] == true }
                            val created = createUser(CreateUserRequest(
                                username = username,
                                brokerOrgId = brokerId.takeIf { it.isNotBlank() },
                                roles = roles.ifEmpty { null }
                            ))
                            onCreated(created)
                        } catch (e: Exception) {
                            error = "Error creating user: ${e.message}"
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
