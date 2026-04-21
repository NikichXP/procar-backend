package com.procar.admin.util

import com.vaadin.flow.component.textfield.TextField

fun validateAddressFields(
    addressField: TextField,
    cityField: TextField,
    stateField: TextField,
    zipCodeField: TextField,
    countryField: TextField,
    timezoneField: TextField
): List<String> {
    val errors = mutableListOf<String>()
    if (addressField.value.isNullOrBlank()) errors.add("Address is required")
    if (cityField.value.isNullOrBlank()) errors.add("City is required")
    if (stateField.value.isNullOrBlank()) errors.add("State is required")
    if (zipCodeField.value.isNullOrBlank()) errors.add("ZIP Code is required")
    if (countryField.value.isNullOrBlank()) errors.add("Country is required")
    if (timezoneField.value.isNullOrBlank()) errors.add("Timezone is required")
    return errors
}
