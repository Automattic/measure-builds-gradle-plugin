pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = "1.9.10"

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.gradle.plugin-publish") version "1.2.1"
        id("io.gitlab.arturbosch.detekt") version "1.23.3"
    }
}

include(":example", ":plugin")
