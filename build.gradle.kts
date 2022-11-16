group = "com.crypto.advisor"
version = "0.0.1-SNAPSHOT"

plugins {
    java
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

val lombokVersion = "1.18.24"

dependencies {

	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

	compileOnly("org.projectlombok:lombok:$lombokVersion")

	annotationProcessor("org.projectlombok:lombok:$lombokVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
