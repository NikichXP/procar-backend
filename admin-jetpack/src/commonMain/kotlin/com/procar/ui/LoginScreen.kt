package com.procar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.procar.api.GatewayConfig
import com.procar.api.login
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (accessToken: String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var useRemoteBackend by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(useRemoteBackend) {
        GatewayConfig.baseUrl = if (useRemoteBackend) {
            "https://api.pc-dev.nikichxp.xyz"
        } else {
            "http://localhost:8080"
        }
    }

    fun submitLogin() {
        if (isLoading) return
        if (username.isBlank() || password.isBlank()) return
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = login(username, password)
                if (result.success && result.accessToken != null) {
                    com.procar.api.AuthState.refreshToken = result.refreshToken ?: ""
                    onLoginSuccess(result.accessToken.token)
                } else {
                    errorMessage = result.message ?: "Login failed"
                }
            } catch (e: ClientRequestException) {
                errorMessage = if (e.response.status == HttpStatusCode.Unauthorized) {
                    "Invalid username or password"
                } else {
                    "Server error: ${e.response.status.description}"
                }
            } catch (e: Exception) {
                errorMessage = "Connection error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
            Card(
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Procar Admin Login",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (useRemoteBackend) "Remote API" else "Localhost (8080)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = useRemoteBackend,
                            onCheckedChange = { useRemoteBackend = it },
                            enabled = !isLoading
                        )
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitLogin() })
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submitLogin() }),
                        enabled = !isLoading
                    )
                    
                    if (errorMessage != null) {
                        Text(
                            errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Button(
                        onClick = { submitLogin() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login")
                        }
                    }
                }
            }
        }
    }
