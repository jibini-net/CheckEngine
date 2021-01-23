import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val lwjglNatives : String
    get() {
        val currentOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()

        return when {
            currentOperatingSystem.isLinux -> System.getProperty("os.arch").let {
                if (it.startsWith("arm") || it.startsWith("aarch64"))
                    "natives-linux-${if (it.contains("64") || it.startsWith("armv8")) "arm64" else "arm32"}"
                else
                    "natives-linux"
            }

            currentOperatingSystem.isMacOsX -> "natives-macos"

            currentOperatingSystem.isWindows ->
                if (System.getProperty("os.arch").contains("64"))
                    "natives-windows"
                else
                    "natives-windows-x86"

            else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
        }
    }

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.4.21"

    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "1.8"

        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += "-include-runtime"
    }
}

dependencies {
    // Kotlin platform and reflection libraries
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    api(fileTree("lib") {
        include("*.jar")
    })

    // Kotlin coroutine multithreading utilities
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.4.2")

    // SLF4J and Log4J libraries
    api("org.slf4j", "slf4j-api", "1.7.30")
    api("org.slf4j", "slf4j-log4j12", "1.7.30")

    // JUnit 5 (Jupiter) test platform
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.7.0")

    // LWJGL core OpenGL and GLFW libraries
    api(platform("org.lwjgl:lwjgl-bom:3.2.3"))

    api("org.lwjgl", "lwjgl")
    api("org.lwjgl", "lwjgl-openal")
    api("org.lwjgl", "lwjgl-opengl")
    api("org.lwjgl", "lwjgl-glfw")

    api("org.lwjgl", "lwjgl-opengles")
    api("org.lwjgl", "lwjgl-egl")

    // Runtime natives for current platform
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)

    runtimeOnly("org.lwjgl", "lwjgl-opengles", classifier = lwjglNatives)

    // Java Open Math Library for physics
    api("org.joml", "joml", "1.10.0")

    // Library for scanning classpath members
    api("io.github.classgraph", "classgraph", "4.8.98")
    // Library for loading and saving JSON-ified objects
    api("com.google.code.gson", "gson", "2.8.6")
}