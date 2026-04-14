package com.procar.core.service

import com.procar.core.config.StorageProperties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Service
class StorageService(
    private val s3: S3AsyncClient,
    private val props: StorageProperties
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun upload(filename: String, contentType: String, bytes: ByteArray): String {
        val extension = filename.substringAfterLast('.', missingDelimiterValue = "")
        val key = if (extension.isNotEmpty()) "${Uuid.generateV7()}.$extension" else "${Uuid.generateV7()}"

        val request = PutObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(bytes.size.toLong())
            .build()

        s3.putObject(request, AsyncRequestBody.fromBytes(bytes)).await()
        return key
    }

    suspend fun download(key: String): Pair<String, Flow<ByteArray>> {
        val request = GetObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .build()

        val response = s3.getObject(request, AsyncResponseTransformer.toBytes()).await()
        val contentType = response.response().contentType() ?: "application/octet-stream"

        val dataFlow = flow<ByteArray> {
            emit(response.asByteArray())
        }

        return contentType to dataFlow
    }
}
