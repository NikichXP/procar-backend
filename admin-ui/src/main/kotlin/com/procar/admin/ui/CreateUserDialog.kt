package com.procar.admin.ui

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
import java.util.UUID

class CreateUserDialog(
    private val onUserCreated: (User) -> Unit
) {

    private val dialog = Dialog()
    private val usernameField = TextField("Username")
    private val emailField = TextField("Email")
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        dialog.headerTitle = "Create User"
        dialog.width = "400px"
        dialog.height = "350px"

        val infoText = Span("Enter user details to create a new user")
        infoText.style.set("color", "var(--lumo-error-text-color)")
        infoText.style.set("font-size", "small")

        usernameField.isRequired = true
        emailField.isRequired = true

        val form = VerticalLayout(infoText, usernameField, emailField)
        form.width = "100%"
        form.isPadding = true

        val createButton = Button("Create User") { onCreateUser() }
        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(createButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER

        dialog.add(form, buttonLayout)
    }

    fun open() {
        dialog.addDetachListener { componentScope.cancel() }
        dialog.open()
        usernameField.focus()
    }

    private fun onCreateUser() {
        val username = usernameField.value
        val email = emailField.value

        if (username.isBlank() || email.isBlank()) {
            Notification.show("Please enter both username and email")
            return
        }

        val currentUI = UI.getCurrent()

        componentScope.launch {
            try {
                //TODO: Implement actual user creation via API
                val newUser = User(
                    id = UUID.randomUUID().toString(),
                    username = username,
                    email = email,
                    createdAt = Instant.now(),
                    status = UserStatus.ACTIVE,
                    lastLogin = null
                )
                
                currentUI.access {
                    Notification.show("User created successfully: $username")
                    dialog.close()
                    onUserCreated(newUser)
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error creating user: ${error.message}")
                }
            }
        }
    }
}
