plugins {
    kotlin("jvm") version BuildPluginsVersion.KOTLIN apply false
    kotlin("plugin.serialization") version BuildPluginsVersion.KOTLIN apply false
    id("io.gitlab.arturbosch.detekt") version BuildPluginsVersion.DETEKT
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    detekt {
        config = rootProject.files("config/detekt/detekt.yml")
        reports {
            html {
                enabled = true
                destination = file("build/reports/detekt.html")
            }
        }
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.register("preMerge") {
    description = "Runs all the tests/verification tasks on both top level and included build."

    dependsOn(":plugin:check")
    dependsOn(":plugin:validatePlugins")
}
