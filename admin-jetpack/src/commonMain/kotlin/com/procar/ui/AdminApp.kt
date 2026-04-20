package com.procar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.AuthState
import com.procar.api.getAccessToken

enum class Screen { LOTS, WAREHOUSES, USERS, BROKERS }

@Composable
fun AdminApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (AuthState.refreshToken.isNotEmpty()) {
            try {
                val accessToken = getAccessToken(AuthState.refreshToken)
                AuthState.accessToken = accessToken.token
                isAuthenticated = true
            } catch (e: Exception) {
                // Refresh token is invalid or expired, clear it and show login
                AuthState.refreshToken = ""
            }
        }
        isCheckingAuth = false
    }

    MaterialTheme {
        if (isCheckingAuth) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!isAuthenticated) {
            LoginScreen(onLoginSuccess = { token -> AuthState.accessToken = token; isAuthenticated = true })
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                var currentScreen by remember { mutableStateOf(Screen.LOTS) }
                NavSidebar(currentScreen = currentScreen, onNavigate = { currentScreen = it }, onLogout = { isAuthenticated = false; AuthState.refreshToken = "" })
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        Screen.LOTS -> LotsScreen()
                        Screen.WAREHOUSES -> WarehousesScreen()
                        Screen.USERS -> UsersScreen()
                        Screen.BROKERS -> BrokersScreen()
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
        NavItem("Brokers", currentScreen == Screen.BROKERS) { onNavigate(Screen.BROKERS) }
        
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
