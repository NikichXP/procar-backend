package com.procar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.procar.api.uploadLotPhoto
import com.procar.model.AdminLotResponse
import com.procar.model.AdminVehicleImageResponse
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
    // Newly uploaded images during this dialog session (parent lot prop is not refreshed live).
    val addedImages = remember(lot.id) { mutableStateListOf<AdminVehicleImageResponse>() }

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

    val allImages = lot.vehicle.images + addedImages
    if (allImages.isEmpty()) {
        Text("No photos yet.", style = MaterialTheme.typography.bodySmall)
    } else {
        allImages.forEach { img ->
            DetailRow(
                label = if (img.isPrimary) "Primary (${img.type})" else img.type,
                value = img.description?.takeIf { it.isNotBlank() }?.let { "$it — ${img.url}" } ?: img.url,
            )
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
