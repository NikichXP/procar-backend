import org.gradle.jvm.tasks.Jar

plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

// Enable bootJar task
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	enabled = true
	archiveClassifier.set("")
}

// Disable regular jar task
tasks.named<Jar>("jar") {
	enabled = false
}

group = "com.procar"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    // Internal API dependency
    implementation(project(":auction-provider-api"))
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")
    testImplementation("org.slf4j:slf4j-api")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // Cucumber
    testImplementation("io.cucumber:cucumber-java:7.15.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.15.0")
    testImplementation("io.cucumber:cucumber-spring:7.15.0")
    testImplementation("io.cucumber:cucumber-junit:7.15.0")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
