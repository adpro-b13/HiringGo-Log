val jsonwebtokenVersion = "0.12.3"
val postgresqlVersion = "42.5.0"
val jakartaPersistenceApiVersion = "3.1.0"
val jakartaValidationApiVersion = "3.0.2"
plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "6.0.1.5171"
}

sonar {
  properties {
    property("sonar.projectKey", "HiringGo-B13-Log-Service")
    property("sonar.projectName", "HiringGo B13 Log Service")
    property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
  }
}

group = "id.ac.ui.cs.advprog.b13.hiringgo.log"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")  // if you need REST API capabilities
    implementation("org.springframework.boot:spring-boot-starter-logging") // for logging
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator") // Added Actuator
    implementation("io.micrometer:micrometer-registry-prometheus") // Added Micrometer Prometheus Registry
    implementation("org.springframework.boot:spring-boot-starter-security") // Added Spring Security
    implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceApiVersion")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    implementation("com.h2database:h2")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion") // Updated version
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion") // Updated version, or jjwt-gson if you prefer Gson
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test") // Added for security testing
    testImplementation("com.h2database:h2") // Added H2 for testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    filter {
        excludeTestsMatching("*FunctionalTest")
    }

    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport{
    reports {
        xml.required.set(true)
        html.required.set(true) // Keep this if you use the HTML report
    }
    dependsOn(tasks.test) // report is always generated after tests run
}