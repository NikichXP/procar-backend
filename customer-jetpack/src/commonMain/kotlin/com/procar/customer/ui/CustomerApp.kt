@file:OptIn(ExperimentalMaterial3Api::class)

package com.procar.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.procar.customer.ui.navigation.Screen
import com.procar.customer.ui.screens.LotDetailScreen
import com.procar.customer.ui.screens.LotFeedScreen
import com.procar.customer.ui.theme.CustomerTheme
import kotlinx.coroutines.launch

@Composable
fun CustomerApp() {
    CustomerTheme {
        AppNavigator()
    }
}

@Composable
private fun AppNavigator() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Feed) }

    when (val screen = currentScreen) {
        is Screen.Feed -> MainScaffold(
            onLotClick = { lotId -> currentScreen = Screen.LotDetail(lotId) },
        )
        is Screen.LotDetail -> LotDetailScreen(
            lotId = screen.lotId,
            onBack = { currentScreen = Screen.Feed },
        )
    }
}

@Composable
private fun MainScaffold(onLotClick: (String) -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(onClose = { scope.launch { drawerState.close() } })
        },
    ) {
        Scaffold(
            topBar = {
                AppTopBar(onMenuClick = { scope.launch { drawerState.open() } })
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                LotFeedScreen(onLotClick = onLotClick)
            }
        }
    }
}

@Composable
private fun AppTopBar(onMenuClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Procar",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            ProfileButton()
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun ProfileButton() {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = "Profile",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun AppDrawer(onClose: () -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
        ) {
            DrawerHeader()
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                label = { Text("Browse Lots") },
                selected = true,
                icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                onClick = onClose,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun DrawerHeader() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "P",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Procar",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            "Car auction marketplace",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
