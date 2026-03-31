package com.procar.admin.ui

import com.procar.auth.api.dto.AuthResult
import com.procar.auth.api.dto.AccessToken
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.VaadinIcon
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class User(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: Instant,
    val status: UserStatus,
    val lastLogin: Instant?
)

enum class UserStatus {
    ACTIVE, BANNED, PENDING
}

class UserGrid(users: MutableList<User>, onOptionsClick: (User) -> Unit) : Grid<User>() {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    init {
        setItems(users)
        addColumn(User::id).setHeader("ID")
        addColumn(User::username).setHeader("Username")
        addColumn(User::email).setHeader("Email")
        addColumn { it.createdAt.atZone(ZoneId.systemDefault()).format(DATE_FORMATTER) }.setHeader("Created At")
        addColumn(User::status).setHeader("Status")
        addColumn { it.lastLogin?.atZone(ZoneId.systemDefault())?.format(DATE_FORMATTER) ?: "Never" }.setHeader("Last Login")
        
        addComponentColumn { user ->
            Button("Options") {
                onOptionsClick(user)
            }.apply {
                icon = VaadinIcon.COG.create()
                addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY)
            }
        }.setHeader("Actions")
    }
}
