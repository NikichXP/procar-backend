package com.procar.core.api

import com.procar.core.service.ImageService
import com.procar.core.service.StorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*

@Tag(name = "file-api", description = "File upload and download")
@RestController
@RequestMapping("/files")
class FileAPI(
    private val imageService: ImageService,
    private val storageService: StorageService
) {

    @Operation(summary = "Upload a file")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun upload(@RequestPart("file") filePart: FilePart): FileUploadResponse {
        val key = storageService.upload(filePart)
        return FileUploadResponse(key)
    }

    @Operation(summary = "Download a file by key")
    @GetMapping("/{key}")
    suspend fun download(@PathVariable key: String): ResponseEntity<Flow<DataBuffer>> {
        val (contentType, dataFlow) = storageService.download(key)

        val bufferFlow = dataFlow.map { bytes ->
            DefaultDataBufferFactory.sharedInstance.wrap(bytes) as DataBuffer
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(bufferFlow)
    }

    @Operation(summary = "Download a resized image by key and size")
    @GetMapping("/{originalKey}/{size}")
    suspend fun downloadResized(
        @PathVariable originalKey: String,
        @PathVariable size: String
    ): ResponseEntity<Flow<DataBuffer>> {
        val sizeInt = size.toIntOrNull()
            ?: return ResponseEntity.notFound().build()

        val lastDotIndex = originalKey.lastIndexOf('.')
        if (lastDotIndex == -1) {
            return ResponseEntity.notFound().build()
        }

        val baseName = originalKey.substring(0, lastDotIndex)
        val extension = originalKey.substring(lastDotIndex + 1)
        val resizedKey = "$baseName.$size.$extension"
        val formatName = extension.lowercase()

        if (storageService.fileExists(resizedKey)) {
            return download(resizedKey)
        }

        if (storageService.fileExists(originalKey)) {
            val (contentType, dataFlow) = storageService.download(originalKey)
            val resizedBytes = imageService.resizeImage(dataFlow, sizeInt, formatName)
            storageService.uploadBytes(resizedKey, resizedBytes, contentType)

            val bufferFlow = flow {
                emit(resizedBytes)
            }.map { bytes ->
                DefaultDataBufferFactory.sharedInstance.wrap(bytes) as DataBuffer
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bufferFlow)
        }

        return ResponseEntity.notFound().build()
    }
}

data class FileUploadResponse(val key: String)
