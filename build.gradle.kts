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
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"

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

kapt {
    annotationProcessor("net.jibini.check.annotation.EngineObjectProcessor")
}

sourceSets {
    main {
        java {
            if (File("${buildDir.absolutePath}/generated/source/kapt/main").exists())
                srcDir("${buildDir.absolutePath}/generated/source/kapt/main")
            if (File("${buildDir.absolutePath}/generated/source/kapt/test").exists())
                srcDir("${buildDir.absolutePath}/generated/source/kapt/test")
        }
    }
}

dependencies {
    // Kotlin platform and reflection libraries
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    // SLF4J and Log4J libraries
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.slf4j", "slf4j-log4j12", "1.7.30")

    // JUnit 5 (Jupiter) test platform
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.7.0")

    // LWJGL core OpenGL and GLFW libraries
    implementation(platform("org.lwjgl:lwjgl-bom:3.2.3"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-glfw")

    // Runtime natives for current platform
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)

    // Java Open Math Library for physics
    implementation("org.joml", "joml", "1.9.25")

    // JUnit 5 (Jupiter) test platform library
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.7.0")

    // Include annotation processing module
    implementation(project(path = ":annotation", configuration = "default"))
    kapt(project(":annotation"))
}