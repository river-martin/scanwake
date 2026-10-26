
// Top-level build file where you can add configuration options common to all subprojects/modules.

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    kotlin("jvm") version "2.4.21" apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("sync") {
    group = "build"
    description = "Task to allow running 'gradle sync'"
}


tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xno-call-assertions",
            "-Xno-param-assertions"
        )
    }
}
