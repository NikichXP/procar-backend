package com.procar.admin.ui

import com.procar.admin.util.validateAddressFields
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
        errors.addAll(validateAddressFields(addressField, cityField, stateField, zipCodeField, countryField, timezoneField))
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
