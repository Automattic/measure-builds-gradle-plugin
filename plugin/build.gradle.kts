plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java-gradle-plugin")
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

group = "com.automattic.android"

project.afterEvaluate{
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["kotlin"])
                group = "com.automattic"
                artifactId = "measure-builds"
            }
        }
    }
}

gradlePlugin {
    plugins.register("measure-builds") {
        id = "com.automattic.android.measure-builds"
        implementationClass = "com.automattic.android.measure.BuildTimePlugin"
    }
}
