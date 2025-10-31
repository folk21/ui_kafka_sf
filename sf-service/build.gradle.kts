
plugins {
  id("java")
  id("org.springframework.boot") version "3.3.4"
  id("io.spring.dependency-management") version "1.1.6"
}
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
repositories { mavenCentral() }
dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.test { useJUnitPlatform() }
