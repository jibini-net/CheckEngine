plugins {
    kotlin("jvm")

    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.squareup", "kotlinpoet", "1.7.1")
}
