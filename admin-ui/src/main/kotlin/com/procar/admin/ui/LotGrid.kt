package com.procar.admin.ui

import com.procar.provider.admin.AdminLotResponse
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.VaadinIcon

class LotGrid(lots: MutableList<AdminLotResponse>, onOptionsClick: (AdminLotResponse) -> Unit) : Grid<AdminLotResponse>() {

    init {
        setItems(lots)
        addColumn(AdminLotResponse::id).setHeader("ID")
        addColumn(AdminLotResponse::title).setHeader("Title")
        addColumn(AdminLotResponse::status).setHeader("Status")
        addColumn { it.auction.currentBid }.setHeader("Current Bid")
        
        addComponentColumn { lot ->
            Button("Options") {
                onOptionsClick(lot)
            }.apply {
                icon = VaadinIcon.COG.create()
                addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY)
            }
        }.setHeader("Actions")
    }
}
