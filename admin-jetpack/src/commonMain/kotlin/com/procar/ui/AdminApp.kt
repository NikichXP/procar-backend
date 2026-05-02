@file:OptIn(ExperimentalMaterial3Api::class)

package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.AuthState
import com.procar.api.getAccessToken
import com.procar.ui.screens.BrokersScreen
import com.procar.ui.screens.LotsScreen
import com.procar.ui.screens.LoginScreen
import com.procar.ui.screens.UsersScreen
import com.procar.ui.screens.WarehousesScreen
import kotlinx.coroutines.launch

enum class Screen(val label: String) {
    LOTS("Lots"),
    WAREHOUSES("Warehouses"),
    USERS("Users"),
    BROKERS("Brokers"),
}

@Composable
fun AdminApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (AuthState.refreshToken.isNotEmpty()) {
            try {
                val accessToken = getAccessToken(AuthState.refreshToken)
                AuthState.accessToken = accessToken.token
                isAuthenticated = true
            } catch (_: Exception) {
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
            AuthenticatedLayout(onLogout = { isAuthenticated = false; AuthState.refreshToken = "" })
        }
    }
}

@Composable
private fun AuthenticatedLayout(onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf(Screen.LOTS) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun toggleDrawer() {
        scope.launch {
            if (drawerState.isOpen) drawerState.close() else drawerState.open()
        }
    }

    fun navigate(screen: Screen) {
        currentScreen = screen
        scope.launch { drawerState.close() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentScreen.label) },
                navigationIcon = {
                    IconButton(onClick = { toggleDrawer() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerItems(currentScreen, ::navigate, onLogout)
                }
            }
        ) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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

@Composable
private fun DrawerItems(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight().padding(vertical = 16.dp)) {
        Text(
            "Procar Admin",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Screen.entries.forEach { screen ->
            NavigationDrawerItem(
                label = { Text(screen.label) },
                selected = currentScreen == screen,
                onClick = { onNavigate(screen) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text("Logout")
        }
    }
}
