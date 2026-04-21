package com.procar.core.api

import com.procar.core.service.StorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
}

data class FileUploadResponse(val key: String)
