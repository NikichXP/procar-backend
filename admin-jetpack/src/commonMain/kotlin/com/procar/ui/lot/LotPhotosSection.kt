package com.procar.ui.lot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.procar.api.absoluteImageUrl
import com.procar.api.deleteLotPhoto
import com.procar.api.imageLoader
import com.procar.api.uploadLotPhoto
import com.procar.gateway.api.dto.AdminLotResponse
import com.procar.gateway.api.dto.AdminVehicleImageResponse
import com.procar.ui.components.SectionHeader
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

/**
 * Lot photos section: lists existing images and lets the user add more.
 *
 * Uses FileKit (multiplatform) so file selection works identically on wasmJs,
 * desktop (JVM), Android and iOS without any expect/actual code.
 *
 * The gateway's /api/admin/lots/{id}/photo endpoint accepts one file per call,
 * so multi-file selection is implemented by looping through the picked files.
 * This is invisible to the user.
 */
@Composable
fun PhotosSection(
    lot: AdminLotResponse,
    onUploaded: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val addedImages = remember(lot.id) { mutableStateListOf<AdminVehicleImageResponse>() }
    val removedUrls = remember(lot.id) { mutableStateListOf<String>() }
    var previewUrl by remember { mutableStateOf<String?>(null) }
    var confirmDeleteUrl by remember { mutableStateOf<String?>(null) }
    var deletingUrl by remember { mutableStateOf<String?>(null) }

    val picker = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(),
        title = "Select photos",
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        scope.launch {
            uploading = true
            error = null
            try {
                files.forEachIndexed { idx, file ->
                    progress = "Uploading ${idx + 1}/${files.size}: ${file.name}"
                    val bytes = file.readBytes()
                    val updated = uploadLotPhoto(
                        lotId = lot.id,
                        fileName = file.name,
                        contentType = guessImageContentType(file.name),
                        bytes = bytes,
                    )
                    // Append only the last (new) image; existing ones are already displayed.
                    updated.vehicle.images.lastOrNull()?.let { addedImages.add(it) }
                }
                progress = null
                onUploaded()
            } catch (e: Exception) {
                error = "Upload failed: ${e.message}"
            } finally {
                uploading = false
            }
        }
    }

    SectionHeader("Photos")

    val allImages = (lot.vehicle.images + addedImages).filterNot { it.url in removedUrls }
    if (allImages.isEmpty()) {
        Text("No photos yet.", style = MaterialTheme.typography.bodySmall)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            allImages.forEach { img ->
                PhotoRow(
                    image = img,
                    deleting = deletingUrl == img.url,
                    onView = { previewUrl = img.url },
                    onDelete = { confirmDeleteUrl = img.url },
                )
            }
        }
    }

    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    progress?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            enabled = !uploading,
            onClick = { picker.launch() },
        ) {
            if (uploading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Uploading…")
            } else {
                Text("Add Photos")
            }
        }
    }

    previewUrl?.let { url ->
        PhotoPreviewDialog(url = url, onDismiss = { previewUrl = null })
    }

    confirmDeleteUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { if (deletingUrl == null) confirmDeleteUrl = null },
            title = { Text("Delete photo?") },
            text = { Text("This will remove the photo from the lot and delete the uploaded file. This cannot be undone.") },
            confirmButton = {
                Button(
                    enabled = deletingUrl == null,
                    onClick = {
                        scope.launch {
                            deletingUrl = url
                            error = null
                            try {
                                deleteLotPhoto(lot.id, url)
                                removedUrls.add(url)
                                confirmDeleteUrl = null
                                onUploaded()
                            } catch (e: Exception) {
                                error = "Delete failed: ${e.message}"
                            } finally {
                                deletingUrl = null
                            }
                        }
                    },
                ) {
                    if (deletingUrl == url) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Deleting…")
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = deletingUrl == null,
                    onClick = { confirmDeleteUrl = null },
                ) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PhotoRow(
    image: AdminVehicleImageResponse,
    deleting: Boolean,
    onView: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        AsyncImage(
            model = absoluteImageUrl(image.url),
            imageLoader = imageLoader,
            contentDescription = image.description,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onView),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (image.isPrimary) "Primary (${image.type})" else image.type,
                style = MaterialTheme.typography.bodyMedium,
            )
            image.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Text(image.url, style = MaterialTheme.typography.labelSmall)
        }
        OutlinedButton(onClick = onView) { Text("View") }
        OutlinedButton(enabled = !deleting, onClick = onDelete) {
            if (deleting) CircularProgressIndicator(modifier = Modifier.size(14.dp)) else Text("Delete")
        }
    }
}

@Composable
private fun PhotoPreviewDialog(url: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Photo preview") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth().height(480.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = absoluteImageUrl(url),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(480.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Best-effort content-type from filename extension. */
private fun guessImageContentType(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        "svg" -> "image/svg+xml"
        else -> "application/octet-stream"
    }
}
