package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.provider.admin.AdminWarehouseResponse
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WarehouseView(
    private val gatewayClientService: GatewayClientService
) : VerticalLayout() {

    private val warehouses = mutableListOf<AdminWarehouseResponse>()
    private val warehouseGrid = WarehouseGrid(warehouses)
    private var initialLoaded = false

    init {
        setPadding(false)
        setSpacing(false)

        val createButton = Button("Create Warehouse") {
            CreateWarehouseDialog(gatewayClientService, onWarehouseCreated = { loadWarehouses() }).open()
        }
        val refreshButton = Button("Refresh") {
            loadWarehouses()
            Notification.show("Warehouses refreshed")
        }

        val actions = HorizontalLayout(createButton, refreshButton)
        add(H2("Warehouses"), warehouseGrid, actions)
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        if (!initialLoaded) {
            initialLoaded = true
            loadWarehouses(attachEvent.ui)
        }
    }

    fun loadWarehouses(currentUI: UI? = UI.getCurrent()) {
        if (currentUI == null) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val list = gatewayClientService.getWarehouses()
                currentUI.access {
                    warehouses.clear()
                    warehouses.addAll(list)
                    warehouseGrid.dataProvider.refreshAll()
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error loading warehouses: ${error.message}")
                }
            }
        }
    }
}
