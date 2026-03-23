package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.provider.admin.AdminLotResponse
import com.procar.provider.bid.ProviderBid
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@Route("")
class AdminView(
    private val gatewayClientService: GatewayClientService
) : VerticalLayout() {
    
    private val users = mutableListOf<String>()
    private val lots = mutableListOf<AdminLotResponse>()
    private val bids = mutableListOf<ProviderBid>()
    
    init {
        addClassName("admin-view")
        defaultHorizontalComponentAlignment = Alignment.CENTER
        
        add(H1("Procar Admin Dashboard"))
        
        // Load initial data
        loadData()
        
        add(createUserSection())
        add(createLotSection())
        add(createBidSection())
    }
    
    private fun createUserSection(): Component {
        val layout = VerticalLayout()
        layout.addClassName("section")
        
        layout.add(H2("Users"))
        
        val userGrid = Grid<String>()
        userGrid.setItems(users)
        userGrid.addColumn { user -> user }.setHeader("User")
        
        val addButton = Button("Add User") { showAddUserDialog() }
        val refreshButton = Button("Refresh") { 
            loadUsers()
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
        
        val lotGrid = Grid<AdminLotResponse>()
        lotGrid.setItems(lots)
        lotGrid.addColumn(AdminLotResponse::id).setHeader("ID")
        lotGrid.addColumn(AdminLotResponse::title).setHeader("Title")
        lotGrid.addColumn(AdminLotResponse::status).setHeader("Status")
        lotGrid.addColumn { it.auction.currentBid }.setHeader("Current Bid")
        
        val addButton = Button("Add Lot") { showAddLotDialog() }
        val refreshButton = Button("Refresh") { 
            loadLots()
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
        
        val bidGrid = Grid<ProviderBid>()
        bidGrid.setItems(bids)
        bidGrid.addColumn(ProviderBid::id).setHeader("ID")
        bidGrid.addColumn(ProviderBid::lotId).setHeader("Lot ID")
        bidGrid.addColumn(ProviderBid::bidderId).setHeader("User ID")
        bidGrid.addColumn(ProviderBid::amount).setHeader("Amount")
        
        val addButton = Button("Add Bid") { showAddBidDialog() }
        val refreshButton = Button("Refresh") { 
            loadBids()
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
            val newUser = nameField.value
            users.add(newUser)
            Notification.show("User added: $newUser")
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
            // TODO: Implement lot creation with proper AdminCreateLotRequest
            Notification.show("Lot creation to be implemented")
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
            // TODO: Implement bid creation
            Notification.show("Bid creation to be implemented")
            dialog.close()
        }
        
        val cancelButton = Button("Cancel") { dialog.close() }
        
        dialog.add(form, saveButton, cancelButton)
        dialog.open()
    }
    
    private fun loadData() {
        loadUsers()
        loadLots()
        loadBids()
    }
    
    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userList = gatewayClientService.getUsers().toList()
                UI.getCurrent().access {
                    users.clear()
                    users.addAll(userList)
                }
            } catch (error: Exception) {
                UI.getCurrent().access {
                    Notification.show("Error loading users: ${error.message}")
                }
            }
        }
    }
    
    private fun loadLots() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lotList = gatewayClientService.getLots().toList()
                UI.getCurrent().access {
                    lots.clear()
                    lots.addAll(lotList)
                }
            } catch (error: Exception) {
                UI.getCurrent().access {
                    Notification.show("Error loading lots: ${error.message}")
                }
            }
        }
    }
    
    private fun loadBids() {
        // For now, load bids for first lot if available
        if (lots.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bidList = gatewayClientService.getBidsForLot(lots.first().id).toList()
                    UI.getCurrent().access {
                        bids.clear()
                        bids.addAll(bidList)
                    }
                } catch (error: Exception) {
                    UI.getCurrent().access {
                        Notification.show("Error loading bids: ${error.message}")
                    }
                }
            }
        }
    }
}
