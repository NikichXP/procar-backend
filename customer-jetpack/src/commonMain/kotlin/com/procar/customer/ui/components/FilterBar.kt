package com.procar.customer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.customer.ui.state.FilterState
import com.procar.gateway.api.dto.CarCondition

private val SortOptions = listOf(
    "endTime,asc" to "Ending soon",
    "endTime,desc" to "Ending last",
    "currentBid,asc" to "Price: Low → High",
    "currentBid,desc" to "Price: High → Low",
)

@Composable
fun FilterBar(
    state: FilterState,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Filters", style = MaterialTheme.typography.titleSmall)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    ConditionFilter(state)
                    SortFilter(state)
                    PriceRangeFilter(state)
                    Button(
                        onClick = {
                            onApply()
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionFilter(state: FilterState) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(null) + CarCondition.entries
    val selectedLabel = state.condition?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Any"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Condition") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { condition ->
                DropdownMenuItem(
                    text = { Text(condition?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Any") },
                    onClick = { state.condition = condition; expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortFilter(state: FilterState) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = SortOptions.firstOrNull { it.first == state.sort }?.second ?: "Ending soon"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sort by") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { state.sort = value; expanded = false },
                )
            }
        }
    }
}

@Composable
private fun PriceRangeFilter(state: FilterState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.priceFrom,
            onValueChange = { state.priceFrom = it },
            label = { Text("Min price") },
            prefix = { Text("$") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = state.priceTo,
            onValueChange = { state.priceTo = it },
            label = { Text("Max price") },
            prefix = { Text("$") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
}
