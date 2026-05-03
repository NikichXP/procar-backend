import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("com.android.application")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.1.21"
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.procar"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvmToolchain(21)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "customer-jetpack"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "customer-jetpack.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 9998
                    static(rootDirPath)
                    static(projectDirPath)
                }
            }
        }
        binaries.executable()
    }

    jvm("desktop")

    androidTarget()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":gateway-api"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation("io.ktor:ktor-client-core:3.1.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
                implementation("io.ktor:ktor-client-logging:3.1.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("io.coil-kt.coil3:coil-compose:3.0.4")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")
            }
        }

        wasmJsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.1.3")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:3.1.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
                implementation("org.slf4j:slf4j-simple:2.0.9")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.10.0")
                implementation("io.ktor:ktor-client-okhttp:3.1.3")
            }
        }
    }
}

android {
    namespace = "com.procar.customer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.procar.customer"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

compose.desktop {
    application {
        mainClass = "com.procar.customer.MainKt"
    }
}
