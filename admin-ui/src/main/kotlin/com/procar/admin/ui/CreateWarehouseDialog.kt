package com.procar.admin.ui

import com.procar.admin.service.GatewayClientService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateWarehouseDialog(
    private val gatewayClientService: GatewayClientService,
    private val onWarehouseCreated: () -> Unit
) {

    private val dialog = Dialog()
    private val form = CreateWarehouseForm()

    init {
        dialog.headerTitle = "Add New Warehouse"
        dialog.width = "700px"

        val saveButton = Button("Save") { onSave() }
        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER

        dialog.add(form, buttonLayout)
    }

    fun open() {
        dialog.open()
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
                gatewayClientService.createWarehouse(request)
                currentUI?.access {
                    Notification.show("Warehouse created successfully")
                    dialog.close()
                    onWarehouseCreated()
                }
            } catch (error: Exception) {
                currentUI?.access {
                    Notification.show("Error creating warehouse: ${error.message}")
                }
            }
        }
    }
}
