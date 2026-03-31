package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.provider.admin.AdminLotResponse
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@Route("")
class AdminView(
    private val gatewayClientService: GatewayClientService
) : AppLayout() {

    private val lots = mutableListOf<AdminLotResponse>()
    private val lotGrid = LotGrid(lots) { lot ->
        LotOptionsDialog(lot, onLotUpdated = { updatedLot ->
            //TODO: Handle lot updates if needed
        }).open()
    }
    private var initialLotsLoaded = false
    
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val lotView = buildLotSection()
    private val warehouseView = WarehouseView(gatewayClientService)
    private val authView = AuthView(gatewayClientService)

    init {
        val title = H1("Procar Admin")
        title.style.set("font-size", "var(--lumo-font-size-l)")
        title.style.set("margin", "0")

        addToNavbar(DrawerToggle(), title)

        val lotsNavButton = Button("Lots") { showLots() }
        lotsNavButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        lotsNavButton.style.set("width", "100%")
        lotsNavButton.style.set("justify-content", "flex-start")

        val warehousesNavButton = Button("Warehouses") { showWarehouses() }
        warehousesNavButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        warehousesNavButton.style.set("width", "100%")
        warehousesNavButton.style.set("justify-content", "flex-start")

        val authNavButton = Button("Users") { showAuth() }
        authNavButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        authNavButton.style.set("width", "100%")
        authNavButton.style.set("justify-content", "flex-start")

        val nav = VerticalLayout(lotsNavButton, warehousesNavButton, authNavButton)
        nav.setPadding(false)
        nav.setSpacing(false)
        addToDrawer(nav)

        setContent(lotView)
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        if (!initialLotsLoaded) {
            initialLotsLoaded = true
            loadLots(attachEvent.ui)
        }
    }
    
    override fun onDetach(detachEvent: com.vaadin.flow.component.DetachEvent) {
        super.onDetach(detachEvent)
        componentScope.cancel()
    }

    private fun showLots() {
        setContent(lotView)
    }

    private fun showWarehouses() {
        setContent(warehouseView)
        warehouseView.loadWarehouses()
    }

    private fun showAuth() {
        setContent(authView)
    }

    private fun buildLotSection(): VerticalLayout {
        val layout = VerticalLayout()
        layout.addClassName("section")

        val createLotButton = Button("Create Lot") {
            CreateLotDialog(gatewayClientService, onLotCreated = { loadLots() }).open()
        }

        val refreshButton = Button("Refresh") {
            loadLots()
            Notification.show("Lots refreshed")
        }

        val actions = HorizontalLayout(createLotButton, refreshButton)
        layout.add(H2("Lots"), lotGrid, actions)
        return layout
    }

    private fun loadLots(currentUI: UI? = UI.getCurrent()) {
        if (currentUI == null) return

        componentScope.launch {
            try {
                val lotList = gatewayClientService.getLots().toList()
                currentUI.access {
                    lots.clear()
                    lots.addAll(lotList)
                    lotGrid.dataProvider.refreshAll()
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error loading lots: ${error.message}")
                }
            }
        }
    }
}
