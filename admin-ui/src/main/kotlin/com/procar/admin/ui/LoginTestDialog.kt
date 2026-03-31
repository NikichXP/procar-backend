package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.auth.api.dto.LoginRequest
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant

class LoginTestDialog(
    private val gatewayClientService: GatewayClientService
) {

    private val dialog = Dialog()
    private val usernameField = TextField("Username")
    private val passwordField = TextField("Password")
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        dialog.headerTitle = "Test Login"
        dialog.width = "400px"
        dialog.height = "300px"

        val infoText = Span("Enter credentials to test login functionality")
        infoText.style.set("color", "var(--lumo-error-text-color)")
        infoText.style.set("font-size", "small")

        usernameField.isRequired = true
        passwordField.isRequired = true

        val form = VerticalLayout(infoText, usernameField, passwordField)
        form.width = "100%"
        form.setPadding(true)

        val testButton = Button("Test Login") { onTestLogin() }
        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(testButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER

        dialog.add(form, buttonLayout)
    }

    fun open() {
        dialog.addDetachListener { componentScope.cancel() }
        dialog.open()
        usernameField.focus()
    }

    private fun onTestLogin() {
        val username = usernameField.value
        val password = passwordField.value

        if (username.isBlank() || password.isBlank()) {
            Notification.show("Please enter both username and password")
            return
        }

        val request = LoginRequest(username, password)
        val currentUI = UI.getCurrent()

        componentScope.launch {
            try {
                val result = gatewayClientService.login(request)
                currentUI.access {
                    if (result.success) {
                        Notification.show("Login successful for user: $username")
                    } else {
                        Notification.show("Login failed: ${result.message}")
                    }
                    
                    dialog.close()
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error during login test: ${error.message}")
                    dialog.close()
                }
            }
        }
    }
}
