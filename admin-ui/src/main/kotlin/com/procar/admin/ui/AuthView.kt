package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.time.Instant

class AuthView(
    private val gatewayClientService: GatewayClientService
) : VerticalLayout() {

    private val users = mutableListOf<User>()
    private val userGrid = UserGrid(users) { user ->
        UserOptionsDialog(user, onUserBanned = { bannedUser ->
            updateUser(bannedUser)
        }).open()
    }
    private var initialLoaded = false
    
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        setPadding(false)
        setSpacing(false)

        val createUserButton = Button("Create User") {
            CreateUserDialog(onUserCreated = { newUser ->
                addUser(newUser)
            }).open()
        }

        val refreshButton = Button("Refresh") {
            loadUsers()
            Notification.show("Users refreshed")
        }

        val actions = HorizontalLayout(createUserButton, refreshButton)
        add(H2("Users Management"), userGrid, actions)
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        if (!initialLoaded) {
            initialLoaded = true
            loadUsers()
        }
    }
    
    override fun onDetach(detachEvent: com.vaadin.flow.component.DetachEvent) {
        super.onDetach(detachEvent)
        componentScope.cancel()
    }

    private fun loadUsers() {
        //TODO: Implement actual user loading via API
        // For now, add some dummy users
        users.clear()
        users.addAll(
            listOf(
                User(
                    id = "1",
                    username = "admin",
                    email = "admin@example.com",
                    createdAt = Instant.now().minusSeconds(86400),
                    status = UserStatus.ACTIVE,
                    lastLogin = Instant.now().minusSeconds(3600)
                ),
                User(
                    id = "2",
                    username = "john_doe",
                    email = "john@example.com",
                    createdAt = Instant.now().minusSeconds(172800),
                    status = UserStatus.ACTIVE,
                    lastLogin = Instant.now().minusSeconds(7200)
                ),
                User(
                    id = "3",
                    username = "jane_smith",
                    email = "jane@example.com",
                    createdAt = Instant.now().minusSeconds(259200),
                    status = UserStatus.BANNED,
                    lastLogin = null
                )
            )
        )
        userGrid.dataProvider.refreshAll()
    }

    private fun addUser(user: User) {
        users.add(0, user) // Add to the beginning
        userGrid.dataProvider.refreshAll()
    }

    private fun updateUser(updatedUser: User) {
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index >= 0) {
            users[index] = updatedUser
            userGrid.dataProvider.refreshAll()
        }
    }
}
