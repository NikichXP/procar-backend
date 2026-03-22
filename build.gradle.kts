plugins {
	id("org.springframework.boot") version "3.3.0" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	kotlin("jvm") version "2.2.21" apply false
	kotlin("plugin.spring") version "2.2.21" apply false
}

group = "com.procar"
version = "0.0.1-SNAPSHOT"
description = "Sales app for Procar"

repositories {
	mavenCentral()
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "io.spring.dependency-management")
	
	group = rootProject.group
	version = rootProject.version
	
	// Set consistent JAR naming
	tasks.withType<Jar> {
		archiveBaseName.set("procar-${project.name}")
		archiveVersion.set(rootProject.version.toString())
	}
	
	extensions.configure<JavaPluginExtension> {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(21))
		}
	}
	
	repositories {
		mavenCentral()
	}
	
	tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		compilerOptions {
			freeCompilerArgs.addAll(listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property"))
		}
	}
	
	tasks.withType<Test> {
		useJUnitPlatform()
	}
}
