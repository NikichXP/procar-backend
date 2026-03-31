package com.procar.admin.ui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class UserOptionsDialog(
    private val user: User,
    private val onUserBanned: (User) -> Unit
) {

    private val dialog = Dialog()

    init {
        dialog.headerTitle = "User Options"
        dialog.width = "400px"
        dialog.height = "300px"

        val userInfo = H3("User: ${user.username}")
        userInfo.style.set("margin-bottom", "20px")

        val userInfoDetails = VerticalLayout(
            Span("ID: ${user.id}"),
            Span("Email: ${user.email}"),
            Span("Status: ${user.status}")
        )
        userInfoDetails.setSpacing(false)

        val banButton = Button("Ban User") {
            //TODO: Implement actual ban functionality
            Notification.show("User ${user.username} has been banned")
            val bannedUser = user.copy(status = UserStatus.BANNED)
            onUserBanned(bannedUser)
            dialog.close()
        }.apply {
            style.set("background-color", "var(--lumo-error-color)")
            style.set("color", "var(--lumo-contrast-color)")
        }

        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(banButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER
        buttonLayout.style.set("margin-top", "20px")

        val content = VerticalLayout(userInfo, userInfoDetails, buttonLayout)
        content.setPadding(true)
        content.setSpacing(true)

        dialog.add(content)
    }

    fun open() {
        dialog.open()
    }
}
