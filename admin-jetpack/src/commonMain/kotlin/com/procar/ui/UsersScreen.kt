package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.model.AdminUser
import com.procar.model.UserStatus
import kotlinx.coroutines.launch
import kotlin.random.Random

private fun dummyUsers() = listOf(
    AdminUser("1", "admin", "admin@example.com", UserStatus.ACTIVE, "2025-01-01", "2025-06-01"),
    AdminUser("2", "john_doe", "john@example.com", UserStatus.ACTIVE, "2025-02-01", "2025-05-20"),
    AdminUser("3", "jane_smith", "jane@example.com", UserStatus.BANNED, "2025-03-01", null),
)

@Composable
fun UsersScreen() {
    var users by remember { mutableStateOf(dummyUsers()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<AdminUser?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Users", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showCreateDialog = true }) { Text("Create User") }
            OutlinedButton(onClick = { users = dummyUsers() }) { Text("Refresh") }
        }

        Spacer(Modifier.height(8.dp))

        DataTable(
            headers = listOf("ID", "Username", "Email", "Status", "Last Login", "Actions"),
            rows = users,
            rowKey = { it.id },
            cellContent = { user, col ->
                when (col) {
                    0 -> Text(user.id, style = MaterialTheme.typography.bodySmall)
                    1 -> Text(user.username)
                    2 -> Text(user.email)
                    3 -> StatusChip(user.status)
                    4 -> Text(user.lastLogin ?: "Never")
                    5 -> TextButton(onClick = { selectedUser = user }) { Text("Options") }
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = { newUser ->
                users = listOf(newUser) + users
                showCreateDialog = false
            }
        )
    }

    selectedUser?.let { user ->
        UserOptionsDialog(
            user = user,
            onDismiss = { selectedUser = null },
            onBanned = { banned ->
                users = users.map { if (it.id == banned.id) banned else it }
                selectedUser = null
            }
        )
    }
}

@Composable
fun StatusChip(status: UserStatus) {
    val color = when (status) {
        UserStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        UserStatus.BANNED -> MaterialTheme.colorScheme.error
        UserStatus.PENDING -> MaterialTheme.colorScheme.secondary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun UserOptionsDialog(user: AdminUser, onDismiss: () -> Unit, onBanned: (AdminUser) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User: ${user.username}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ID: ${user.id}", style = MaterialTheme.typography.bodySmall)
                Text("Email: ${user.email}")
                Text("Status: ${user.status}")
                Text("Last Login: ${user.lastLogin ?: "Never"}")
            }
        },
        confirmButton = {
            Button(
                onClick = { onBanned(user.copy(status = UserStatus.BANNED)) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = user.status != UserStatus.BANNED
            ) { Text("Ban User") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CreateUserDialog(onDismiss: () -> Unit, onCreated: (AdminUser) -> Unit) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(360.dp)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(username, { username = it }, label = { Text("Username *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email *") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (username.isBlank() || email.isBlank()) {
                    error = "Please enter both username and email"
                    return@Button
                }
                onCreated(AdminUser(
                    id = Random.nextInt(1000, 9999).toString(),
                    username = username,
                    email = email,
                    status = UserStatus.ACTIVE,
                    createdAt = "2025-06-01",
                    lastLogin = null,
                ))
            }) { Text("Create") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
