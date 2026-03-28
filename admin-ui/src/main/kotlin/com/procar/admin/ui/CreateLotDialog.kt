package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateLotDialog(
    private val gatewayClientService: GatewayClientService,
    private val onLotCreated: () -> Unit
) {

    private val dialog = Dialog()
    private val form = CreateLotForm()

    init {
        dialog.headerTitle = "Add New Lot"
        dialog.width = "900px"
        dialog.height = "800px"

        val infoText = Span("Fields marked with * are required")
        infoText.style.set("color", "var(--lumo-error-text-color)")
        infoText.style.set("font-size", "small")

        val scrollWrapper = VerticalLayout(form)
        scrollWrapper.width = "100%"
        scrollWrapper.height = "650px"
        scrollWrapper.style.set("overflow", "auto")

        val saveButton = Button("Save") { onSave() }
        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER

        dialog.add(infoText, scrollWrapper, buttonLayout)
    }

    fun open() {
        loadWarehouses()
        dialog.open()
    }

    private fun loadWarehouses() {
        val currentUI = UI.getCurrent()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val warehouses = gatewayClientService.getWarehouses()
                currentUI?.access {
                    form.setWarehouses(warehouses)
                }
            } catch (error: Exception) {
                currentUI?.access {
                    Notification.show("Warning: could not load warehouses: ${error.message}")
                }
            }
        }
    }

    private fun onSave() {
        val errors = form.validate()
        if (errors.isNotEmpty()) {
            val message = if (errors.size == 1) errors.first()
            else "Please fix the following issues:\n${errors.joinToString("\n") { "• $it" }}"
            Notification.show(message)
            return
        }

        val request = form.buildRequest()
        val currentUI = UI.getCurrent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val createdLot = gatewayClientService.createLot(request)
                currentUI.access {
                    Notification.show("Lot created successfully: ${createdLot.id}")
                    dialog.close()
                    onLotCreated()
                }
            } catch (error: Exception) {
                currentUI.access {
                    Notification.show("Error creating lot: ${error.message}")
                }
            }
        }
    }
}
