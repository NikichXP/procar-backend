package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.auth.api.dto.RegisterRequest
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class RegisterTestDialog(
    private val gatewayClientService: GatewayClientService
) {

    private val dialog = Dialog()
    private val userIdField = TextField("User ID")
    private val usernameField = TextField("Username")
    private val passwordField = TextField("Password")
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        dialog.headerTitle = "Test Register"
        dialog.width = "400px"
        dialog.height = "350px"

        val infoText = Span("Enter user details to test registration functionality")
        infoText.style.set("color", "var(--lumo-error-text-color)")
        infoText.style.set("font-size", "small")

        userIdField.isRequired = true
        userIdField.placeholder = "Generated automatically if empty"
        usernameField.isRequired = true
        passwordField.isRequired = true

        val form = VerticalLayout(infoText, userIdField, usernameField, passwordField)
        form.width = "100%"
        form.isPadding = true

        val testButton = Button("Test Register") { onTestRegister() }
        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(testButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER

        dialog.add(form, buttonLayout)
    }

    fun open() {
        dialog.addDetachListener { componentScope.cancel() }
        dialog.open()
        userIdField.value = UUID.randomUUID().toString()
        usernameField.focus()
    }

    private fun onTestRegister() {
        val userId = userIdField.value.ifBlank { UUID.randomUUID().toString() }
        val username = usernameField.value
        val password = passwordField.value

        if (username.isBlank() || password.isBlank()) {
            Notification.show("Please enter both username and password")
            return
        }

        val request = RegisterRequest(userId, username, password)
        val currentUI = UI.getCurrent()

        componentScope.launch {
            try {
                val result = gatewayClientService.register(request)
                currentUI.access {
                    if (result.success) {
                        Notification.show("Registration successful for user: $username")
                    } else {
                        Notification.show("Registration failed: ${result.message}")
                    }
                    
                    dialog.close()
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error during registration test: ${error.message}")
                    dialog.close()
                }
            }
        }
    }
}
