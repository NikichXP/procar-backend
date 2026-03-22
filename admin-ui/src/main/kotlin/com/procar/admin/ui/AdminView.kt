package com.procar.admin.ui

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment

data class StubUser(val id: String, val name: String, val email: String, val role: String)
data class StubLot(val id: String, val title: String, val status: String, val price: Double)
data class StubBid(val id: String, val lotId: String, val userId: String, val amount: Double)

@Route("")
class AdminView : VerticalLayout() {
    
    private val users = mutableListOf(
        StubUser("1", "John Doe", "john@example.com", "ADMIN"),
        StubUser("2", "Jane Smith", "jane@example.com", "USER"),
        StubUser("3", "Bob Johnson", "bob@example.com", "USER")
    )
    
    private val lots = mutableListOf(
        StubLot("1", "2023 BMW M3", "ACTIVE", 75000.0),
        StubLot("2", "2022 Mercedes C63", "ACTIVE", 68000.0),
        StubLot("3", "2021 Audi RS5", "SOLD", 62000.0)
    )
    
    private val bids = mutableListOf(
        StubBid("1", "1", "2", 72000.0),
        StubBid("2", "1", "3", 73000.0),
        StubBid("3", "2", "2", 67000.0)
    )
    
    init {
        addClassName("admin-view")
        defaultHorizontalComponentAlignment = Alignment.CENTER
        
        add(H1("Procar Admin Dashboard"))
        
        add(createUserSection())
        add(createLotSection())
        add(createBidSection())
    }
    
    private fun createUserSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")
        
        layout.add(H2("Users"))
        
        val userGrid = Grid(StubUser::class.java)
        userGrid.setItems(users)
        userGrid.setColumns("id", "name", "email", "role")
        
        val addButton = Button("Add User") { showAddUserDialog() }
        val refreshButton = Button("Refresh") { 
            userGrid.setItems(users)
            Notification.show("Users refreshed")
        }
        
        layout.add(userGrid, addButton, refreshButton)
        return layout
    }
    
    private fun createLotSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")
        
        layout.add(H2("Lots"))
        
        val lotGrid = Grid(StubLot::class.java)
        lotGrid.setItems(lots)
        lotGrid.setColumns("id", "title", "status", "price")
        
        val addButton = Button("Add Lot") { showAddLotDialog() }
        val refreshButton = Button("Refresh") { 
            lotGrid.setItems(lots)
            Notification.show("Lots refreshed")
        }
        
        layout.add(lotGrid, addButton, refreshButton)
        return layout
    }
    
    private fun createBidSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")
        
        layout.add(H2("Bids"))
        
        val bidGrid = Grid(StubBid::class.java)
        bidGrid.setItems(bids)
        bidGrid.setColumns("id", "lotId", "userId", "amount")
        
        val addButton = Button("Add Bid") { showAddBidDialog() }
        val refreshButton = Button("Refresh") { 
            bidGrid.setItems(bids)
            Notification.show("Bids refreshed")
        }
        
        layout.add(bidGrid, addButton, refreshButton)
        return layout
    }
    
    private fun showAddUserDialog() {
        val dialog = Dialog()
        dialog.setHeaderTitle("Add New User")
        
        val nameField = TextField("Name")
        val emailField = TextField("Email")
        val roleField = TextField("Role")
        
        val form = FormLayout(nameField, emailField, roleField)
        
        val saveButton = Button("Save") {
            val newUser = StubUser(
                id = (users.size + 1).toString(),
                name = nameField.value,
                email = emailField.value,
                role = roleField.value
            )
            users.add(newUser)
            Notification.show("User added: ${newUser.name}")
            dialog.close()
        }
        
        val cancelButton = Button("Cancel") { dialog.close() }
        
        dialog.add(form, saveButton, cancelButton)
        dialog.open()
    }
    
    private fun showAddLotDialog() {
        val dialog = Dialog()
        dialog.setHeaderTitle("Add New Lot")
        
        val titleField = TextField("Title")
        val statusField = TextField("Status")
        val priceField = TextField("Price")
        
        val form = FormLayout(titleField, statusField, priceField)
        
        val saveButton = Button("Save") {
            val newLot = StubLot(
                id = (lots.size + 1).toString(),
                title = titleField.value,
                status = statusField.value,
                price = priceField.value.toDoubleOrNull() ?: 0.0
            )
            lots.add(newLot)
            Notification.show("Lot added: ${newLot.title}")
            dialog.close()
        }
        
        val cancelButton = Button("Cancel") { dialog.close() }
        
        dialog.add(form, saveButton, cancelButton)
        dialog.open()
    }
    
    private fun showAddBidDialog() {
        val dialog = Dialog()
        dialog.setHeaderTitle("Add New Bid")
        
        val lotIdField = TextField("Lot ID")
        val userIdField = TextField("User ID")
        val amountField = TextField("Amount")
        
        val form = FormLayout(lotIdField, userIdField, amountField)
        
        val saveButton = Button("Save") {
            val newBid = StubBid(
                id = (bids.size + 1).toString(),
                lotId = lotIdField.value,
                userId = userIdField.value,
                amount = amountField.value.toDoubleOrNull() ?: 0.0
            )
            bids.add(newBid)
            Notification.show("Bid added: $${newBid.amount}")
            dialog.close()
        }
        
        val cancelButton = Button("Cancel") { dialog.close() }
        
        dialog.add(form, saveButton, cancelButton)
        dialog.open()
    }
}
