package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.procar.provider.admin.AdminLotResponse
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Route("")
class AdminView(
    private val gatewayClientService: GatewayClientService
) : VerticalLayout() {

    private val lots = mutableListOf<AdminLotResponse>()
    private val lotGrid = LotGrid(lots)
    private var initialLotsLoaded = false

    init {
        runBlocking {
            gatewayClientService.getLots().collect { lots.add(it) }
        }

        addClassName("admin-view")
        defaultHorizontalComponentAlignment = Alignment.CENTER

        add(H1("Procar Lots"))
        add(buildLotSection())
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        if (!initialLotsLoaded) {
            initialLotsLoaded = true
            loadLots(attachEvent.ui)
        }
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

        CoroutineScope(Dispatchers.IO).launch {
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
