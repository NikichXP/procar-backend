package com.procar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.model.AdminUpdateLotRequest

/**
 * A single editable field that tracks an "original" value and a "current" value,
 * allowing UIs to highlight modifications, revert, and commit after a successful save.
 */
class FieldState<T>(initial: T) {
    var original by mutableStateOf(initial)
        private set
    var current by mutableStateOf(initial)
    val isModified: Boolean get() = current != original

    fun revert() { current = original }
    fun commit() { original = current }
}

/**
 * A section of editable fields that can validate itself and produce a partial
 * [AdminUpdateLotRequest]. Implementations delegate modified/revert/commit to their
 * inner [FieldState]s.
 */
interface SectionEditState {
    val isModified: Boolean
    fun revert()
    fun commit()
    fun validate(): String?
    fun buildRequest(): AdminUpdateLotRequest
}

/** Read-only label/value row used in detail views; hidden when value is null/blank. */
@Composable
fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(140.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Outlined text field bound to a [FieldState], visually highlighted when modified. */
@Composable
fun EditableTextField(
    state: FieldState<String>,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = state.current,
        onValueChange = { state.current = it },
        label = { Text(label) },
        colors = modifiedFieldColors(state.isModified),
        modifier = modifier,
    )
}

/** Enum dropdown bound to a [FieldState], visually highlighted when modified. */
@Composable
fun EditableEnumField(
    state: FieldState<String>,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    EnumDropdown(
        label = label,
        selected = state.current,
        options = options,
        modifier = modifier,
        modified = state.isModified,
        onSelected = { state.current = it },
    )
}

/** Per-section action row (Revert + Save) that is only rendered when the section has changes. */
@Composable
fun SectionActions(
    modified: Boolean,
    saving: Boolean,
    onRevert: () -> Unit,
    onSave: () -> Unit,
) {
    if (!modified) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(enabled = !saving, onClick = onRevert) { Text("Revert") }
        Button(enabled = !saving, onClick = onSave) {
            if (saving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
            else Text("Save")
        }
    }
}
