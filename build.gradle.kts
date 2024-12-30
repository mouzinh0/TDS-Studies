
plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google() // for compose
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(compose.desktop.currentOs)

    // MongoDB driver
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.23.1")
    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.2.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    // If needed for UI + coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.1")
}

compose.desktop {
    application {
        mainClass = "ui.CheckersAppKt"
    }
}