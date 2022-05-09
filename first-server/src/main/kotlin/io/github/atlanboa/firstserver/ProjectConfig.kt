package io.github.atlanboa.firstserver

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ProjectConfig {

    @Value("\${cloud.aws.credentials.accessKey}")
    private val accessKey: String? = null

    @Value("\${cloud.aws.credentials.secretKey}")
    private val secretKey: String? = null

    @Value("\${cloud.aws.region.static}")
    private val region: String? = null

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder().build()
    }

    @Bean
    fun s3Client(): AmazonS3 {
        val credentials = BasicAWSCredentials(this.accessKey, this.secretKey)
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(this.region)
            .enablePayloadSigning()
            .build()
    }
}
