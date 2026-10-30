import com.android.build.api.dsl.ApplicationExtension
import java.security.MessageDigest
import java.io.File

android {
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencyLocking {
    lockAllConfigurations()
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

extensions.configure<ApplicationExtension> {
    namespace = "io.github.river_martin.scanwake"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.river_martin.scanwake"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // optimization block removed to prevent R8 bytecode corruption on Kotlin collections / Lifecycle
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

androidComponents {
    val versionName = extensions.getByType<ApplicationExtension>().defaultConfig.versionName
        ?: error("versionName must be set in defaultConfig")
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("ScanWake-v$versionName.apk")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Material 3 Window Size Class & Adaptive Layouts
    implementation(libs.androidx.compose.windowsizeclass)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)

    // Room Database
    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Barcode Scanning
    implementation(libs.zxing.cpp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register("generateChecksums") {
    notCompatibleWithConfigurationCache("Custom script task reading build outputs")
    doLast {
        val searchDirs = listOf(
            File(project.projectDir, "release")
        )
        var found = false
        searchDirs.forEach { dir ->
            if (dir.exists()) {
                dir.listFiles { f -> f.extension == "apk" }?.forEach { apkFile ->
                    val bytes = apkFile.readBytes()
                    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
                    val sha256 = digest.joinToString("") { b: Byte -> "%02x".format(b) }
                    val checksumFile = File(apkFile.parentFile, "checksums.txt")
                    checksumFile.writeText("$sha256  ${apkFile.name}\n")
                    println("Generated SHA-256 checksum for ${apkFile.name}: $sha256 at ${checksumFile.absolutePath}")
                    found = true
                }
            }
        }
        if (!found) {
            println("Warning: No release APK found in build outputs or app/release directory.")
        }
    }
}

afterEvaluate {
    tasks.findByName("signReleaseApk")?.finalizedBy("generateChecksums")
    tasks.findByName("packageRelease")?.finalizedBy("generateChecksums")
}
