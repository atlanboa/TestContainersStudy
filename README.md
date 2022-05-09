# Why?
* 로컬 테스트를 좀 더 효과적으로 해보자.

# Dev Enviroment
* Kotlin
* Spring Boot
* [TestContainer](https://www.testcontainers.org/)
* [Rest Assured](https://rest-assured.io/)


# Git
* [Repository](https://github.com/atlanboa/TestContainersStudy.git)


# 무엇을 할 것인가?
* AWS S3 연동 로직 테스트
* MicroService 테스트 흉내내기
* Local Docker Image Build
* Build 한 Docker Image 로 TestContainer 띄우기
* Rest Assured 로 쿵짝쿵짝

# 시작
[Spring Initializer](https://start.spring.io/) 에서 프로젝트 2개 생성

### Setting
* 각 프로젝트는 아래처럼 구성해서 생성, 이름은 맘대로
![](https://velog.velcdn.com/images/devsh/post/867c474e-166c-4700-9227-d4317ce981a2/image.png)

## Simple Api Server
### build.gradle.kts
* 프로젝트 생성 후에 _**build.gradle.kts**_ 는 아래처럼
```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "3.1.2"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "io.github.atlanboa"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    jib {
        from {
            image = "azul/zulu-openjdk-alpine:11.0.13"
        }

        to {
            image = "docker.akmj.io/${rootProject.name}"
            tags = mutableSetOf("${project.version}", "latest")
            credHelper = "osxkeychain"
        }

        container {
            mainClass = "io.github.atlanboa.simpleapiserver.SimpleApiServerApplication"
            volumes = mutableListOf("/tmp")
        }
    }
}

```
* jib 는 로컬 docker image build 하기 위해서 필요



### Controller

api test 를 위해서 controller 에 method 하나 생성
```kotlin
@RestController
@RequestMapping("/simple")
class Controller {

    @Value("\${akmj.name}")
    private lateinit var author: String

    @GetMapping
    fun getAnyRequest(@RequestParam objectName: String): ResponseEntity<String> {
        return ResponseEntity.ok("this is any response with $objectName by $author")
    }
}
```

### SimpleApiServerApplication
app 실행 ain method 수정
```kotlin
@SpringBootApplication
class SimpleApiServerApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SimpleApiServerApplication>(*args)
        }
    }
}

```
### application.yaml
port 값 할당
testContainer 생성시에 환경 변수 주입을 위해서 akmj.name 하나 추가
```yaml
server:
  port: 8889

akmj:
  name: ${AUTHOR_NAME:"akmj"}
```

### local docker image build
로컬 도커 이미지 빌드하려면, 로컬에서 도커가 실행되어야 함.
![](https://velog.velcdn.com/images/devsh/post/d0b66c2a-97be-4466-bb1a-4e67f4bc9ae8/image.png)

**_build.gradle.kts_** 에  jib 추가하면 jib gradle task 가 생김.
여기서 _**jibDockerBuild**_ 실행

### docker image 확인
terminal 띄워서 docker images 쳐서 해당 이미지 있는지 확인.
![](https://velog.velcdn.com/images/devsh/post/4834310e-6021-45bc-b6b1-5e8fe5203ed2/image.png)

다음처럼 생성됬으면 simple api 은 끝


## First Server

### build.gradle.kts
```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "io.github.atlanboa"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // annotation
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // test container
    implementation(platform("org.testcontainers:testcontainers-bom:1.16.2"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:localstack")

    // rest assured
    testImplementation("io.rest-assured:kotlin-extensions:4.4.0")

    // aws
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

```


* TestContainers 와 LocalStackService 를 쓰려면 요놈들이 필요함
```kotlin
    // test container
    implementation(platform("org.testcontainers:testcontainers-bom:1.16.2"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.testcontainers:localstack")
```

* rest assured 은 요놈
```kotlin 
    testImplementation("io.rest-assured:kotlin-extensions:4.4.0")
```

* aws service 를 사용하려면 요놈
```kotlin
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
```

### application.yaml
* port 설정
* bean overriding
* simple api url property
* s3 bucket set up
* aws logging 설정

```yaml
server:
  port: 8888

# s3Client bean 오버라이딩해야함.
spring:
  main:
    allow-bean-definition-overriding: true

# 테스트할 외부 컨테이너 서버 url 필요
akmj:
  simple-api-server:
    url: ${SIMPLE_API_SERVER_URL:http://localhost:8889/simple}

# aws 셋업
cloud:
  aws:
    s3:
      bucket: fake-bucket
    region:
      static: ap-northeast-2
    credentials:
      accessKey: ACCESSKEY
      secretKey: SECRETKEY
      instance-profile: true
    stack:
      auto: false

# aws s3 access 관련해서 문제없어도 warning 뜨는데, 보기 안좋음. 안뜨게 설정
logging:
  level:
    com:
      amazonaws:
        util:
          EC2MetadataUtils: error       
```

### ProjectConfig
restTemplate 이랑 s3Client Bean 생성해줌.
```kotlin
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
```

### Controller
* 테스트할 컨트롤러 생성
* 로직은 간단하게 bucket object 있는지 확인하고 있으면, simple api server 에 요청해서 String 데이터 반환받기.
```kotlin
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
```


# 테스트 코드 작성합시다.
테스트의 목적은 AWS S3 Storage 를 직접 사용하는게 아닌, LocalStackContainer 로 띄우고, Simple Api Server 연동 테스트 또한 TestContainer 로 띄워 해당 로직 테스트

### IntegrationTestConfig
```kotlin
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class IntegrationTestConfig {

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
```
* 여기서 S3 LocalStackContainer 를 정의하고, s3Client를 LocalStackContainer 의 값으로 셋업해줌.
* LocalStackEndpoint는 container가 start 된 이후에 가져올 수 있음으로 @DependsOn이 반드시 필요함.

### IntegrationTest
해당 클래스에서 정의해야 될 내용은 다음과 같음.
1. 임의로 생성한 Simple Api Server Container
2. First Server 의 Property akmj.simple-api-server.url 값 주입
3, rest assured set up

```kotlin
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
```

* 테스트는 Defined Port 가 아닌 Random Port 로 테스트 하는걸 지향해야 함.
* 테스트 전에 bucket 을 생성하고, object 를 하나 넣어줌.

### 코드 자근자근 뜯어보기
```kotlin
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

```
simple api server 도커 컨테이너를 띄우기 위한 부분.

- container 는 local docker image url 를 사용하여 띄울것이고, port는 8889, simple api server 의 외부 환경 변수 주입값으로 AUTHOR_NAME 넣어주고, container 시작
- first server 에 application.yaml property 를 주입해줌.

#### DynamicPropertySource
* first server 의 환경변수 주입할때 사용하는 이놈은 TestContainers 를 지원하기 위해서 생긴거임..
* static 메소드로 사용해야 된다고 함. 일반 메소드로는 DynamicPropretyRegistry 안받아짐.

```kotlin
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
 ```
 
 - 테스트 전에 bucket set up 해주는 작업
 - requestSpecification set up
 
 #### Test
 ```kotlin
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
 ```
 - 이 로직을 테스트하게 되면, S3 Storage, Simple Api Server 가 Docker Container 로 실행된 이후 해당 API 요청 테스트가 진행이 됨.
 
 
 
