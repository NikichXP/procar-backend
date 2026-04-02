import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

// Disable bootJar task to avoid Gradle 9.x compatibility issues
tasks.named<BootJar>("bootJar") {
	enabled = false
}

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// Enable regular jar task
tasks.named<Jar>("jar") {
	enabled = true
	archiveClassifier.set("")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}
