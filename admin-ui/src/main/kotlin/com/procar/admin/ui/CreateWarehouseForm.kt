package com.procar.admin.ui

import com.procar.provider.admin.AdminCreateWarehouseRequest
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.TextField

class CreateWarehouseForm : FormLayout() {

    val nameField = TextField("Name *")
    val addressField = TextField("Address *")
    val cityField = TextField("City *")
    val stateField = TextField("State *")
    val zipCodeField = TextField("ZIP Code *")
    val countryField = TextField("Country *")
    val timezoneField = TextField("Timezone *")
    val contactNameField = TextField("Contact Name")
    val contactPhoneField = TextField("Contact Phone")
    val contactEmailField = TextField("Contact Email")

    init {
        add(nameField, addressField, cityField, stateField, zipCodeField, countryField, timezoneField, contactNameField, contactPhoneField, contactEmailField)
        timezoneField.value = "UTC"
        width = "100%"
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (nameField.value.isNullOrBlank()) errors.add("Name is required")
        if (addressField.value.isNullOrBlank()) errors.add("Address is required")
        if (cityField.value.isNullOrBlank()) errors.add("City is required")
        if (stateField.value.isNullOrBlank()) errors.add("State is required")
        if (zipCodeField.value.isNullOrBlank()) errors.add("ZIP Code is required")
        if (countryField.value.isNullOrBlank()) errors.add("Country is required")
        if (timezoneField.value.isNullOrBlank()) errors.add("Timezone is required")
        return errors
    }

    fun buildRequest(): AdminCreateWarehouseRequest = AdminCreateWarehouseRequest(
        name = nameField.value,
        address = addressField.value,
        city = cityField.value,
        state = stateField.value,
        zipCode = zipCodeField.value,
        country = countryField.value,
        timezone = timezoneField.value,
        contactName = contactNameField.value.takeUnless { it.isNullOrBlank() },
        contactPhone = contactPhoneField.value.takeUnless { it.isNullOrBlank() },
        contactEmail = contactEmailField.value.takeUnless { it.isNullOrBlank() }
    )
}
