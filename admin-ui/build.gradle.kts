plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

// Set build directory for this module to generated/
layout.buildDirectory.set(file("generated"))

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Vaadin dependencies - simplified
    implementation("com.vaadin:vaadin-spring-boot-starter")
    
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:24.4.6")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
