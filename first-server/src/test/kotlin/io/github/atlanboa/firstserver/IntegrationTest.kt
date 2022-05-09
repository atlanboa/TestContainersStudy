package io.github.atlanboa.firstserver

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.ByteArrayInputStream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [IntegrationTestConfig::class])
internal class IntegrationTest {

    companion object {
        @Container
        val container =
            GenericContainer("docker.akmj.io/simple-api-server:0.0.1-SNAPSHOT")
                .apply {
                    withExposedPorts(8889)
                    withEnv(mutableMapOf("AUTHOR_NAME" to "akmj"))
                    start()
                }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("akmj.simple-api-server.url") {
                "http://${container.containerIpAddress}:${container.getMappedPort(8889)}/simple"
            }
        }
    }

    @Autowired
    private lateinit var s3Client: AmazonS3

    private val objectMapper = ObjectMapper()

    @LocalServerPort
    private var port: Int = 0

    private lateinit var requestSpec: RequestSpecification

    @BeforeAll
    fun setUp() {
        val logConfig = LogConfig.logConfig()
            .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
        val config = RestAssuredConfig.config().logConfig(logConfig)

        this.requestSpec = RequestSpecBuilder()
            .setBaseUri("http://localhost:${this.port}")
            .setContentType(ContentType.JSON)
            .setConfig(config)
            .build()

        val inputStream = ByteArrayInputStream("fake object".toByteArray())

        this.s3Client.createBucket("fake-bucket")
        this.s3Client.putObject("fake-bucket", "object", inputStream, ObjectMetadata())
    }

    @AfterAll
    fun tearDown() {
        RestAssured.reset()
    }

    @Test
    fun `api test`() {
        val msg = Given {
            spec(requestSpec)
        } When {
            get("/first")
        } Then {
            statusCode(HttpStatus.SC_OK)
        } Extract {
            response().asString()
        }

        assertEquals("this is any response with object by akmj", msg)
    }

}