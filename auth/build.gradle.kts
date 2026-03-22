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

dependencies {
	implementation(project(":auth-api"))
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")
    testImplementation("org.slf4j:slf4j-api")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5") 
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
