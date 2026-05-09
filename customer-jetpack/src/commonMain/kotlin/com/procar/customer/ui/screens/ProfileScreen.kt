package com.procar.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.procar.customer.api.AuthToken
import com.procar.customer.api.addToWatchlist
import com.procar.customer.api.fetchCurrentUser
import com.procar.customer.api.fetchUserBids
import com.procar.customer.api.fetchUserWatchlist
import com.procar.customer.api.login
import com.procar.customer.api.register
import com.procar.customer.api.removeFromWatchlist
import com.procar.customer.ui.components.LotCard
import com.procar.customer.ui.state.ProfileState
import com.procar.customer.ui.state.ProfileTab
import com.procar.gateway.api.dto.GatewayUserRegisterRequest
import com.procar.gateway.api.dto.LoginRequest
import com.procar.gateway.api.dto.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLotClick: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state = remember { ProfileState() }
    var isLoggedIn by remember { mutableStateOf(AuthToken.accessToken != null) }

    fun loadProfile() {
        if (!isLoggedIn) return
        scope.launch {
            state.isLoading = true
            state.errorMessage = null
            try {
                state.user = fetchCurrentUser()
            } catch (e: Exception) {
                state.errorMessage = "Failed to load profile: ${e.message}"
                if (e.message?.contains("401") == true || e.message?.contains("Not authenticated") == true) {
                    AuthToken.accessToken = null
                    isLoggedIn = false
                }
            } finally {
                state.isLoading = false
            }
        }
    }

    fun loadBids() {
        if (!isLoggedIn) return
        scope.launch {
            state.bidsLoading = true
            state.bidsError = null
            try {
                val page = fetchUserBids()
                state.bids.clear()
                state.bids.addAll(page.content)
            } catch (e: Exception) {
                state.bidsError = "Failed to load bids: ${e.message}"
            } finally {
                state.bidsLoading = false
            }
        }
    }

    fun loadWatchlist() {
        if (!isLoggedIn) return
        scope.launch {
            state.watchlistLoading = true
            state.watchlistError = null
            try {
                val lots = fetchUserWatchlist()
                state.watchlist.clear()
                state.watchlist.addAll(lots)
            } catch (e: Exception) {
                state.watchlistError = "Failed to load watchlist: ${e.message}"
            } finally {
                state.watchlistLoading = false
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            loadProfile()
            loadBids()
            loadWatchlist()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!isLoggedIn) {
                LoginPrompt(
                    onLoginSuccess = {
                        isLoggedIn = true
                        state.reset()
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                        ProfileTab.entries.forEach { tab ->
                            Tab(
                                selected = state.selectedTab == tab,
                                onClick = { state.selectedTab = tab },
                                text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    when (state.selectedTab) {
                        ProfileTab.PROFILE -> ProfileTabContent(state)
                        ProfileTab.BIDS -> BidsTabContent(state, onLotClick)
                        ProfileTab.WATCHLIST -> WatchlistTabContent(state, onLotClick, onRefresh = { loadWatchlist() })
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginPrompt(onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            if (isRegister) "Create Account" else "Sign In",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            if (isRegister) "Register to start bidding" else "Login to view your profile",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    error = "Please fill in all fields"
                    return@Button
                }
                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        val result = if (isRegister) {
                            register(GatewayUserRegisterRequest(username, password))
                        } else {
                            login(LoginRequest(username, password))
                        }
                        val token = result.accessToken
                        if (result.success && token != null) {
                            AuthToken.accessToken = token.token
                            onLoginSuccess()
                        } else {
                            error = result.message ?: "Authentication failed"
                        }
                    } catch (e: Exception) {
                        error = e.message ?: "Unknown error"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (isRegister) "Register" else "Login")
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { isRegister = !isRegister; error = null }) {
            Text(
                if (isRegister) "Already have an account? Sign in" else "Don't have an account? Register"
            )
        }
    }
}

@Composable
private fun ProfileTabContent(state: ProfileState) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    state.errorMessage?.let { error ->
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val user = state.user
    if (user == null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No user data available")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                user.username.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            user.username,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "ID: ${user.id.take(8)}…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Roles",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    user.roles.forEach { role ->
                        RoleChip(role)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Statistics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(12.dp))
                StatRow("Active Bids", state.bids.count { it.bidStatus.name == "WINNING" || it.bidStatus.name == "OUTBID" }.toString())
                StatRow("Won Auctions", state.bids.count { it.bidStatus.name == "WON" }.toString())
                StatRow("Watchlist", state.watchlist.size.toString())
            }
        }
    }
}

@Composable
private fun RoleChip(role: UserRole) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            role.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun BidsTabContent(state: ProfileState, onLotClick: (String) -> Unit) {
    if (state.bidsLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    state.bidsError?.let { error ->
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    if (state.bids.isEmpty()) {
        EmptyState("You haven't placed any bids yet")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.bids, key = { it.id }) { bid ->
            BidCard(bid = bid, onLotClick = { onLotClick(bid.lotId) })
        }
    }
}

@Composable
private fun BidCard(bid: com.procar.gateway.api.dto.UserBid, onLotClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onLotClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${bid.lot.car.brandName} ${bid.lot.car.modelName}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    "${bid.lot.car.year} · ${bid.lot.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your bid: $${formatWithCommas(bid.amount.toLong())}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                )
            }

            BidStatusChip(bid.bidStatus.name)
        }
    }
}

@Composable
private fun BidStatusChip(status: String) {
    val (color, text) = when (status) {
        "WINNING" -> MaterialTheme.colorScheme.primaryContainer to "Winning"
        "OUTBID" -> MaterialTheme.colorScheme.errorContainer to "Outbid"
        "WON" -> MaterialTheme.colorScheme.tertiaryContainer to "Won"
        "LOST" -> MaterialTheme.colorScheme.surfaceVariant to "Lost"
        else -> MaterialTheme.colorScheme.surfaceVariant to status
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun WatchlistTabContent(
    state: ProfileState,
    onLotClick: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    if (state.watchlistLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    state.watchlistError?.let { error ->
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRefresh) { Text("Retry") }
            }
        }
        return
    }

    if (state.watchlist.isEmpty()) {
        EmptyState("Your watchlist is empty")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.watchlist, key = { it.id }) { lot ->
            LotCard(
                lot = lot,
                onClick = { onLotClick(lot.id) },
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatWithCommas(value: Long): String =
    value.toString().reversed().chunked(3).joinToString(",").reversed()
