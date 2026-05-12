package com.procar.customer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.procar.customer.api.absoluteImageUrl
import com.procar.customer.api.imageLoader
import com.procar.gateway.api.dto.LotStatus
import com.procar.gateway.api.dto.LotSummary
import com.procar.gateway.api.dto.LotType

@Composable
fun LotCard(
    lot: LotSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "card_press_scale",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            LotCardImage(lot)
            LotCardContent(lot)
        }
    }
}

@Composable
private fun LotCardImage(lot: LotSummary) {
    val firstImage = lot.photos.firstOrNull() ?: lot.car.images.firstOrNull()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
    ) {
        if (firstImage != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(absoluteImageUrl(firstImage))
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "${lot.car.brandName} ${lot.car.modelName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                        startY = 80f,
                    )
                )
        )

        LotStatusChip(
            status = lot.status,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
        )
    }
}

@Composable
private fun LotCardContent(lot: LotSummary) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "${lot.car.brandName} ${lot.car.modelName}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = "${lot.car.year} · ${lot.car.condition.name.lowercase().replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(Modifier.height(10.dp))

        LotPriceRow(lot)

        lot.bidsCount?.takeIf { it > 0 }?.let { count ->
            Text(
                text = "$count bid${if (count != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun LotPriceRow(lot: LotSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        when (lot.lotType) {
            LotType.BUYOUT -> {
                lot.buyoutPrice?.let { price ->
                    PriceBlock(label = "Buy now", amount = price)
                }
            }

            LotType.HYBRID -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    lot.currentBid?.let { bid -> PriceBlock(label = "Current bid", amount = bid) }
                    lot.buyoutPrice?.let { price -> PriceBlock(label = "Buy now", amount = price) }
                }
            }

            else -> {
                lot.currentBid?.let { bid ->
                    PriceBlock(label = "Current bid", amount = bid)
                } ?: lot.startingBid?.let { start ->
                    PriceBlock(label = "Starting bid", amount = start)
                }
            }
        }

        lot.endTime?.let { end ->
            Text(
                text = formatEndTime(end),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PriceBlock(label: String, amount: Double) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatPrice(amount),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun LotStatusChip(status: LotStatus, modifier: Modifier = Modifier) {
    val (label, containerColor) = when (status) {
        LotStatus.ACTIVE -> "Live" to Color(0xFF2E7D32)
        LotStatus.PENDING -> "Soon" to Color(0xFFF57F17)
        LotStatus.AWAITING_PAYMENT -> "Won" to Color(0xFF1565C0)
        LotStatus.AWAITING_SHIPMENT -> "Paid" to Color(0xFF6A1B9A)
        LotStatus.IN_TRANSIT -> "Shipping" to Color(0xFF00695C)
        LotStatus.COMPLETED -> "Done" to Color(0xFF546E7A)
        LotStatus.INVALID -> "Invalid" to Color(0xFFB71C1C)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = containerColor.copy(alpha = 0.92f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
        )
    }
}

private fun formatWithCommas(value: Long): String =
    value.toString().reversed().chunked(3).joinToString(",").reversed()

private fun formatPrice(amount: Double): String =
    "$" + formatWithCommas(amount.toLong())

private fun formatEndTime(isoTime: String): String {
    val datePart = isoTime.substringBefore('T')
    return "Ends $datePart"
}
