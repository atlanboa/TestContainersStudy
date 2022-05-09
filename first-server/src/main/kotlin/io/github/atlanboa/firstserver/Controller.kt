package io.github.atlanboa.firstserver

import com.amazonaws.services.s3.AmazonS3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/first")
class Controller @Autowired constructor(
    private val restTemplate: RestTemplate,
    private val s3Client: AmazonS3
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucket: String

    @Value("\${akmj.simple-api-server.url}")
    private lateinit var simpleApiUrl: String

    @GetMapping
    fun getS3ObjectName(): ResponseEntity<String> {
        if (s3Client.doesObjectExist(this.bucket, "object")) {
            val urlBuilder = UriComponentsBuilder
                .fromHttpUrl("$simpleApiUrl?objectName=object")
            val response = restTemplate.exchange(
                urlBuilder.toUriString(),
                HttpMethod.GET,
                HttpEntity(null, null),
                String::class.java
            )
            return ResponseEntity.ok(response.body)
        }
        return ResponseEntity.ok("object name query failed")
    }
}