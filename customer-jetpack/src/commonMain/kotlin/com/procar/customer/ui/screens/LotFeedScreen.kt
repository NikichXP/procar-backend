package com.procar.customer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.customer.api.fetchLots
import com.procar.customer.ui.components.FilterBar
import com.procar.customer.ui.components.LotCard
import com.procar.customer.ui.state.FeedState
import com.procar.customer.ui.state.FilterState
import kotlinx.coroutines.launch

@Composable
fun LotFeedScreen(
    onLotClick: (lotId: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val feedState = remember { FeedState() }
    val filterState = remember { FilterState() }

    fun loadFirstPage() {
        scope.launch {
            feedState.reset()
            feedState.isLoading = true
            try {
                val page = fetchLots(
                    cursor = null,
                    condition = filterState.condition,
                    sort = filterState.sort,
                    priceFrom = filterState.priceFromDouble,
                    priceTo = filterState.priceToDouble,
                )
                feedState.lots.addAll(page.data)
                feedState.nextCursor = page.nextCursor
                feedState.hasMore = page.nextCursor != null
            } catch (e: Exception) {
                feedState.errorMessage = "Failed to load lots: ${e.message}"
            } finally {
                feedState.isLoading = false
            }
        }
    }

    fun loadNextPage() {
        if (!feedState.hasMore || feedState.isLoadingMore || feedState.nextCursor == null) return
        scope.launch {
            feedState.isLoadingMore = true
            try {
                val page = fetchLots(
                    cursor = feedState.nextCursor,
                    condition = filterState.condition,
                    sort = filterState.sort,
                    priceFrom = filterState.priceFromDouble,
                    priceTo = filterState.priceToDouble,
                )
                feedState.lots.addAll(page.data)
                feedState.nextCursor = page.nextCursor
                feedState.hasMore = page.nextCursor != null
            } catch (e: Exception) {
                feedState.errorMessage = "Failed to load more: ${e.message}"
            } finally {
                feedState.isLoadingMore = false
            }
        }
    }

    LaunchedEffect(Unit) { loadFirstPage() }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterBar(
            state = filterState,
            onApply = { loadFirstPage() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        feedState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (feedState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LotList(
                feedState = feedState,
                onLotClick = onLotClick,
                onScrolledToEnd = { loadNextPage() },
            )
        }
    }
}

@Composable
private fun LotList(
    feedState: FeedState,
    onLotClick: (String) -> Unit,
    onScrolledToEnd: () -> Unit,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onScrolledToEnd()
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (feedState.lots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No lots found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            itemsIndexed(feedState.lots, key = { _, lot -> lot.id }) { _, lot ->
                LotCard(lot = lot, onClick = { onLotClick(lot.id) })
            }

            if (feedState.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            }

            if (!feedState.hasMore && feedState.lots.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "You've seen all ${feedState.lots.size} lots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
