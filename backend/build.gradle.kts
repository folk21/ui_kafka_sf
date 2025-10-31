
plugins {
  id("java")
  id("org.springframework.boot") version "3.3.4"
  id("io.spring.dependency-management") version "1.1.6"
}

java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
  compileOnly("org.projectlombok:lombok:1.18.34")
  annotationProcessor("org.projectlombok:lombok:1.18.34")
  testCompileOnly("org.projectlombok:lombok:1.18.34")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("io.micrometer:micrometer-registry-prometheus")

  // JWT
  implementation("io.jsonwebtoken:jjwt-api:0.12.6")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

  // Kafka
  implementation("org.springframework.kafka:spring-kafka")

  // AWS DynamoDB v2
  implementation("software.amazon.awssdk:dynamodb:2.28.24")
  implementation("software.amazon.awssdk:apache-client:2.28.24")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  
  testImplementation("org.springframework.kafka:spring-kafka-test")
  testImplementation("org.testcontainers:junit-jupiter:1.20.3")
  testImplementation("org.testcontainers:kafka:1.20.3")
  testImplementation("org.testcontainers:localstack:1.20.3")

}

tasks.test {
  useJUnitPlatform()
}
