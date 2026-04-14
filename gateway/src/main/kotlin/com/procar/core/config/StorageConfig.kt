package com.procar.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import java.net.URI

@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class StorageConfig {

    @Bean
    fun s3AsyncClient(props: StorageProperties): S3AsyncClient {
        return S3AsyncClient.builder()
            .endpointOverride(URI.create(props.endpoint))
            .region(Region.of(props.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKey, props.secretKey)
                )
            )
            .forcePathStyle(true)
            .build()
    }
}

@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val endpoint: String = "https://nbg1.your-objectstorage.com",
    val region: String = "eu-central",
    val bucket: String = "procar-test",
    val accessKey: String = "",
    val secretKey: String = ""
)
