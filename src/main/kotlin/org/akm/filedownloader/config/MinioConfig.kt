package org.akm.filedownloader.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(
    @Value("\${aws.access-key}") private val accessKeyId: String,
    @Value("\${aws.secret-key}") private val secretKey: String,
    @Value("\${aws.s3.endpoint}") private val endpoint: String
) {

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKeyId, secretKey)
            .build()
    }
}