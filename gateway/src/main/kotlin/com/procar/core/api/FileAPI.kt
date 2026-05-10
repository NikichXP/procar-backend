package com.procar.core.api

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
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Tag(name = "file-api", description = "File upload and download")
@RestController
@RequestMapping("/files")
class FileAPI(
    private val storageService: StorageService
) {

    private fun resizeImage(bytes: ByteArray, size: Int, formatName: String): ByteArray {
        val originalImage = ImageIO.read(ByteArrayInputStream(bytes))
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height

        val scaledWidth: Int
        val scaledHeight: Int

        if (originalWidth > originalHeight) {
            scaledWidth = size
            scaledHeight = (size * originalHeight.toDouble() / originalWidth).toInt()
        } else {
            scaledHeight = size
            scaledWidth = (size * originalWidth.toDouble() / originalHeight).toInt()
        }

        val scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val resizedImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = resizedImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(resizedImage, formatName, outputStream)
        return outputStream.toByteArray()
    }

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
    @GetMapping("/{key}/{size}")
    suspend fun downloadResized(
        @PathVariable key: String,
        @PathVariable size: String
    ): ResponseEntity<Flow<DataBuffer>> {
        val sizeInt = size.toIntOrNull()
            ?: return ResponseEntity.notFound().build()

        val lastDotIndex = key.lastIndexOf('.')
        if (lastDotIndex == -1) {
            return ResponseEntity.notFound().build()
        }

        val baseName = key.substring(0, lastDotIndex)
        val extension = key.substring(lastDotIndex + 1)
        val resizedKey = "$baseName.$size.$extension"
        val formatName = extension.lowercase()

        if (storageService.fileExists(resizedKey)) {
            val (contentType, dataFlow) = storageService.download(resizedKey)
            val bufferFlow = dataFlow.map { bytes ->
                DefaultDataBufferFactory.sharedInstance.wrap(bytes) as DataBuffer
            }
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bufferFlow)
        }

        if (storageService.fileExists(key)) {
            val (contentType, dataFlow) = storageService.download(key)
            val originalBytes = dataFlow.map { it }.toList().first()
            val resizedBytes = resizeImage(originalBytes, sizeInt, formatName)
            storageService.uploadBytes(resizedKey, resizedBytes, contentType)

            val bufferFlow = flow<ByteArray> {
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
