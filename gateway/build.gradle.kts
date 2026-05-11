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
	implementation(project(":auction-provider-api"))
	implementation(project(":auth-api"))
	implementation(project(":gateway-api"))
	implementation(project(":user-api"))
	implementation(project(":commons"))

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("software.amazon.awssdk:s3:2.29.52")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
	}
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
