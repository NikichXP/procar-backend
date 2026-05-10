package com.procar.core.service

import com.procar.core.config.StorageProperties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Service
class StorageService(
    private val s3: S3AsyncClient,
    private val props: StorageProperties
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun upload(filePart: FilePart): String {
        val contentType = filePart.headers().contentType?.toString() ?: "application/octet-stream"
        val buf = filePart.content()
            .reduce { a, b -> a.write(b) }
            .awaitSingle()
        val bytes = ByteArray(buf.readableByteCount())
        buf.read(bytes)

        val extension = filePart.filename().substringAfterLast('.', missingDelimiterValue = "")
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

    suspend fun delete(key: String) {
        val request = DeleteObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .build()
        s3.deleteObject(request).await()
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

    suspend fun fileExists(key: String): Boolean {
        val request = HeadObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .build()
        return try {
            s3.headObject(request).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadBytes(key: String, bytes: ByteArray, contentType: String) {
        val request = PutObjectRequest.builder()
            .bucket(props.bucket)
            .key(key)
            .contentType(contentType)
            .contentLength(bytes.size.toLong())
            .build()

        s3.putObject(request, AsyncRequestBody.fromBytes(bytes)).await()
    }
}
