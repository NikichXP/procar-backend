package com.procar.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.procar.customer.api.absoluteImageUrl
import com.procar.customer.api.fetchLotDetail
import com.procar.customer.api.imageLoader
import com.procar.customer.ui.components.LotStatusChip
import com.procar.gateway.api.dto.Bid
import com.procar.gateway.api.dto.LotDetail
import com.procar.gateway.api.dto.LotType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotDetailScreen(
    lotId: String,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var lot by remember { mutableStateOf<LotDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lotId) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                lot = fetchLotDetail(lotId)
            } catch (e: Exception) {
                errorMessage = "Failed to load lot: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    lot?.let {
                        Text(
                            "${it.car.brandName} ${it.car.modelName}",
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMessage != null -> ErrorState(errorMessage!!, onBack)
                lot != null -> LotDetailContent(lot!!)
            }
        }
    }
}

@Composable
private fun LotDetailContent(lot: LotDetail) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { PhotoGallery(lot.car.images) }
        item { LotHeroSection(lot) }
        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { VehicleDetailsSection(lot) }
        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { LocationSection(lot) }
        if (lot.lotType != LotType.BUYOUT && lot.recentBids.isNotEmpty()) {
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { RecentBidsSection(lot.recentBids) }
        }
        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
        item { FeesSection(lot) }
    }
}

@Composable
private fun PhotoGallery(images: List<String>) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            )
        }
        return
    }

    if (images.size == 1) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(absoluteImageUrl(images.first()))
                .build(),
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(260.dp),
        )
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(images) { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(absoluteImageUrl(imageUrl))
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(300.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}

@Composable
private fun LotHeroSection(lot: LotDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${lot.car.brandName} ${lot.car.modelName}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    "${lot.car.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            LotStatusChip(status = lot.status, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(Modifier.height(16.dp))

        when (lot.lotType) {
            LotType.BUYOUT -> lot.buyoutPrice?.let { price ->
                PriceHighlight(label = "Buy now price", amount = price)
            }
            LotType.HYBRID -> {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    lot.currentBid?.let { bid -> PriceHighlight(label = "Current bid", amount = bid) }
                    lot.buyoutPrice?.let { price -> PriceHighlight(label = "Buy now", amount = price) }
                }
            }
            else -> {
                lot.currentBid?.let { bid ->
                    PriceHighlight(label = "Current bid", amount = bid)
                } ?: lot.startingBid?.let { start ->
                    PriceHighlight(label = "Starting bid", amount = start)
                }
                lot.bidsCount?.let { count ->
                    Text(
                        "$count bid${if (count != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        lot.endTime?.let { end ->
            Spacer(Modifier.height(8.dp))
            InfoChip(label = "Ends", value = end.substringBefore('T').let { "on $it" })
        }

        lot.startTime?.let { start ->
            Spacer(Modifier.height(4.dp))
            InfoChip(label = "Starts", value = start.substringBefore('T').let { "on $it" })
        }

        lot.car.description?.let { desc ->
            Spacer(Modifier.height(12.dp))
            Text(
                desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PriceHighlight(label: String, amount: Double) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            formatPrice(amount),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun VehicleDetailsSection(lot: LotDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        SectionTitle("Vehicle Details")
        Spacer(Modifier.height(8.dp))
        val car = lot.car
        DetailGrid(
            listOfNotNull(
                "Condition" to car.condition.name.lowercase().replaceFirstChar { it.uppercase() },
                car.vin?.let { "VIN" to it },
                car.mileage?.let { "Mileage" to "${formatWithCommas(it.toLong())} mi" },
                car.color?.let { "Color" to it },
                car.engine?.let { "Engine" to it },
                car.transmission?.let { "Transmission" to it.displayName },
                car.drivetrain?.let { "Drivetrain" to it.displayName },
            )
        )
    }
}

@Composable
private fun LocationSection(lot: LotDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        SectionTitle("Location")
        Spacer(Modifier.height(8.dp))
        val loc = lot.location
        Text(
            "${loc.city}, ${loc.state}, ${loc.country}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun FeesSection(lot: LotDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        SectionTitle("Fees")
        Spacer(Modifier.height(8.dp))
        val fees = lot.fees
        if (fees.buyerPremiumPercent == null && fees.documentationFee == null) {
            Text(
                "No fee information available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            DetailGrid(
                listOfNotNull(
                    fees.buyerPremiumPercent?.let { "Buyer premium" to "${it}%" },
                    fees.documentationFee?.let { "Documentation fee" to "$" + formatWithCommas(it.toLong()) },
                )
            )
        }
    }
}

@Composable
private fun RecentBidsSection(bids: List<Bid>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        SectionTitle("Recent Bids")
        Spacer(Modifier.height(8.dp))
        bids.take(5).forEach { bid ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (bid.isWinning) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            if (bid.isWinning) "★" else "·",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (bid.isWinning) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        bid.bidderId.take(8) + "…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    formatPrice(bid.amount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (bid.isWinning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Go back") }
    }
}

private fun formatWithCommas(value: Long): String =
    value.toString().reversed().chunked(3).joinToString(",").reversed()

private fun formatPrice(amount: Double): String =
    "$" + formatWithCommas(amount.toLong())
