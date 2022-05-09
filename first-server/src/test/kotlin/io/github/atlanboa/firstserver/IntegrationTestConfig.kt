package io.github.atlanboa.firstserver

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class IntegrationTestConfig {

//    @Bean
//    fun restTemplateBuilder(): RestTemplateBuilder {
//        return RestTemplateBuilder()
//            .basicAuthentication("akmj", "akmj")
//    }
//
//    @Bean
//    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
//        return restTemplateBuilder.build()
//    }

    @Bean
    @DependsOn("s3Container")
    fun s3Client(localStackContainer: LocalStackContainer): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(LocalStackContainer.Service.S3))
            .withCredentials(localStackContainer.defaultCredentialsProvider)
            .build()
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun s3Container(): LocalStackContainer {
        val localstackImage = DockerImageName.parse("localstack/localstack:0.11.3")
        return LocalStackContainer(localstackImage)
            .withServices(LocalStackContainer.Service.S3)
    }
}