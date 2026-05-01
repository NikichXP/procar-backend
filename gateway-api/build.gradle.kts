import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm")
}

group = "com.procar"
version = "0.0.1-SNAPSHOT"

tasks.named<Jar>("jar") {
	enabled = true
	archiveClassifier.set("")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
}
