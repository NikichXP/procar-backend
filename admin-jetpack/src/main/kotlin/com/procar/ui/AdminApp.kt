package com.procar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Screen { LOTS, WAREHOUSES, USERS }

@Composable
fun AdminApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    
    MaterialTheme {
        if (!isAuthenticated) {
            LoginScreen(onLoginSuccess = { isAuthenticated = true })
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                var currentScreen by remember { mutableStateOf(Screen.LOTS) }
                NavSidebar(currentScreen = currentScreen, onNavigate = { currentScreen = it }, onLogout = { isAuthenticated = false })
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        Screen.LOTS -> LotsScreen()
                        Screen.WAREHOUSES -> WarehousesScreen()
                        Screen.USERS -> UsersScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun NavSidebar(currentScreen: Screen, onNavigate: (Screen) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Procar Admin",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        NavItem("Lots", currentScreen == Screen.LOTS) { onNavigate(Screen.LOTS) }
        NavItem("Warehouses", currentScreen == Screen.WAREHOUSES) { onNavigate(Screen.WAREHOUSES) }
        NavItem("Users", currentScreen == Screen.USERS) { onNavigate(Screen.USERS) }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}
