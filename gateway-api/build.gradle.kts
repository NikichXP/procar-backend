import org.gradle.jvm.tasks.Jar

// Disable bootJar task to avoid Gradle 9.x compatibility issues
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	enabled = false
}

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.procar"
version = "0.0.1-SNAPSHOT"

// Enable regular jar task
tasks.named<Jar>("jar") {
	enabled = true
	archiveClassifier.set("")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}