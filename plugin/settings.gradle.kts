pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://a8c-libs.s3.amazonaws.com/android")
            content {
                includeGroup("com.automattic.android")
                includeGroup("com.automattic.android.publish-to-s3")
            }
        }
    }

    val kotlinVersion = "1.9.10"

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.gradle.plugin-publish") version "1.2.1"
        id("io.gitlab.arturbosch.detekt") version "1.23.3"
        id("com.automattic.android.publish-to-s3") version "0.9.0"
    }
}

rootProject.name = "measure-builds-gradle-plugin"
