import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")

    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += "-include-runtime"
    }
}

dependencies {
    // Kotlin platform and reflection libraries
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    // Include root project and inherit API libs
    api(project(":"))
}