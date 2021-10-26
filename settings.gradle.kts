pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":example")
includeBuild("plugin-build")
