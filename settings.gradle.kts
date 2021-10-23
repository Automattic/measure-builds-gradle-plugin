pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}

include(":example")
includeBuild("plugin-build")
