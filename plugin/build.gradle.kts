plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gradle.plugin-publish")
    id("io.gitlab.arturbosch.detekt")
    id("com.automattic.android.publish-to-s3")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("com.gradle:gradle-enterprise-gradle-plugin:3.15.1")

    implementation("io.ktor:ktor-client-core:1.6.4")
    implementation("io.ktor:ktor-client-cio:1.6.4")
    implementation("io.ktor:ktor-client-logging:1.6.4")
    implementation("io.ktor:ktor-client-serialization:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

gradlePlugin {
    website.set("https://github.com/wzieba/tracks-gradle/")
    vcsUrl.set("https://github.com/wzieba/tracks-gradle/")

    plugins {
        create("tracks.plugin") {
            id = "io.github.wzieba.tracks.plugin"
            implementationClass = "io.github.wzieba.tracks.plugin.BuildTimePlugin"
            displayName = "Gradle plugin which reports build times to Tracks."
            version = "1.2.1"
            description = "Gradle plugin which reports build times to Tracks."
            tags.set(listOf("automattic"))
        }
    }
}

tasks.create("setupPluginUploadFromEnvironment") {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}
