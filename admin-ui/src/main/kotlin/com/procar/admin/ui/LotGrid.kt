package com.procar.admin.ui

import com.procar.provider.admin.AdminLotResponse
import com.vaadin.flow.component.grid.Grid

class LotGrid(lots: MutableList<AdminLotResponse>) : Grid<AdminLotResponse>() {

    init {
        setItems(lots)
        addColumn(AdminLotResponse::id).setHeader("ID")
        addColumn(AdminLotResponse::title).setHeader("Title")
        addColumn(AdminLotResponse::status).setHeader("Status")
        addColumn { it.auction.currentBid }.setHeader("Current Bid")
    }
}
