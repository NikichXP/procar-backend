package com.procar.admin.ui

import com.vaadin.flow.component.notification.Notification

object FormValidationUtils {
    
    /**
     * Validates a form and shows error notifications if validation fails.
     * @return true if validation passes, false if there are errors
     */
    fun validateAndShowErrors(errors: List<String>): Boolean {
        if (errors.isNotEmpty()) {
            val message = if (errors.size == 1) errors.first()
            else "Please fix the following issues:\n${errors.joinToString("\n") { "• $it" }}"
            Notification.show(message)
            return false
        }
        return true
    }
}
