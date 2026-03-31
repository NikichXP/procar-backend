package com.procar.admin.ui

import com.procar.provider.admin.AdminLotResponse
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class LotOptionsDialog(
    private val lot: AdminLotResponse,
    private val onLotUpdated: (AdminLotResponse) -> Unit
) {

    private val dialog = Dialog()

    init {
        dialog.headerTitle = "Lot Options"
        dialog.width = "500px"
        dialog.height = "400px"

        val lotInfo = H3("Lot: ${lot.title}")
        lotInfo.style.set("margin-bottom", "20px")

        val lotDetails = VerticalLayout(
            Span("ID: ${lot.id}"),
            Span("Title: ${lot.title}"),
            Span("Status: ${lot.status}"),
            Span("Current Bid: ${lot.auction.currentBid}")
        )
        lotDetails.setSpacing(false)

        val closeButton = Button("Close Lot") {
            //TODO: Implement actual lot closing functionality
            Notification.show("Lot ${lot.id} has been closed")
            dialog.close()
        }.apply {
            style.set("background-color", "var(--lumo-error-color)")
            style.set("color", "var(--lumo-contrast-color)")
        }

        val cancelButton = Button("Cancel") { dialog.close() }

        val buttonLayout = HorizontalLayout(closeButton, cancelButton)
        buttonLayout.alignItems = Alignment.CENTER
        buttonLayout.style.set("margin-top", "20px")

        val content = VerticalLayout(lotInfo, lotDetails, buttonLayout)
        content.setPadding(true)
        content.setSpacing(true)

        dialog.add(content)
    }

    fun open() {
        dialog.open()
    }
}
