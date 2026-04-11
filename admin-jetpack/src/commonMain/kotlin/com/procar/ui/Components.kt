package com.procar.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    cellContent: @Composable (item: T, columnIndex: Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                .padding(vertical = 8.dp, horizontal = 4.dp),
        ) {
            headers.forEach { header ->
                Text(
                    header,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
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
            LazyColumn {
                itemsIndexed(rows, key = { _, item -> rowKey(item) }) { index, item ->
                    val bgColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    Surface(color = bgColor) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            headers.indices.forEach { colIndex ->
                                Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
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
