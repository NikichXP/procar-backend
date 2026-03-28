package com.procar.admin.ui

import com.procar.provider.admin.AdminWarehouseResponse
import com.vaadin.flow.component.grid.Grid

class WarehouseGrid(warehouses: MutableList<AdminWarehouseResponse>) : Grid<AdminWarehouseResponse>() {

    init {
        setItems(warehouses)
        addColumn(AdminWarehouseResponse::id).setHeader("ID")
        addColumn(AdminWarehouseResponse::name).setHeader("Name")
        addColumn(AdminWarehouseResponse::city).setHeader("City")
        addColumn(AdminWarehouseResponse::state).setHeader("State")
        addColumn(AdminWarehouseResponse::country).setHeader("Country")
        addColumn(AdminWarehouseResponse::timezone).setHeader("Timezone")
        addColumn(AdminWarehouseResponse::contactName).setHeader("Contact")
    }
}
