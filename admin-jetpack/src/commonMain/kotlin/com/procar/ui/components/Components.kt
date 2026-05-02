package com.procar.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

val ModifiedFieldBackground: Color = Color(0xC3FFF59D)

/**
 * Validates standard location fields. Returns the first error message, or null if all are valid.
 * Pass `timezone = null` to skip the timezone check.
 */
fun validateLocationFields(
    address: String,
    city: String,
    state: String,
    zipCode: String,
    country: String,
    timezone: String? = null,
): String? {
    if (address.isBlank()) return "Address is required"
    if (city.isBlank()) return "City is required"
    if (state.isBlank()) return "State is required"
    if (zipCode.isBlank()) return "ZIP code is required"
    if (country.isBlank()) return "Country is required"
    if (timezone != null && timezone.isBlank()) return "Timezone is required"
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun modifiedFieldColors(modified: Boolean): TextFieldColors {
    return if (modified) {
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ModifiedFieldBackground,
            unfocusedContainerColor = ModifiedFieldBackground,
            disabledContainerColor = ModifiedFieldBackground,
        )
    } else {
        OutlinedTextFieldDefaults.colors()
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumDropdown(
    label: String,
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    modified: Boolean = false,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = modifiedFieldColors(modified),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun <T> DataTable(
    headers: List<String>,
    rows: List<T>,
    rowKey: (T) -> Any,
    modifier: Modifier = Modifier.fillMaxWidth(),
    cellContent: @Composable (item: T, columnIndex: Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    val minCellWidth = 160.dp
    val padding = 0.05f
    BoxWithConstraints(modifier = modifier) {
        val colCount = headers.size
        val tableNaturalWidth = minCellWidth * colCount
        val isWide = maxWidth > tableNaturalWidth
        val horizontalPadding = if (isWide) maxWidth * padding else 0.dp
        val availableWidth = maxWidth - horizontalPadding * 2
        val cellWidth = if (colCount > 0) max(minCellWidth, availableWidth / colCount) else minCellWidth
        Column(modifier = Modifier.padding(horizontal = horizontalPadding)) {
            // Header row
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
                headers.forEach { header ->
                    Text(
                        header,
                        modifier = Modifier.width(cellWidth).padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("No data", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(rows, key = { _, item -> rowKey(item) }) { index, item ->
                        val bgColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        Surface(color = bgColor) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                headers.indices.forEach { colIndex ->
                                    Box(modifier = Modifier.width(cellWidth).padding(horizontal = 4.dp)) {
                                        cellContent(item, colIndex)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
