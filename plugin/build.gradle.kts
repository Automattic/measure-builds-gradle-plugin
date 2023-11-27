plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java-gradle-plugin")
    id("io.gitlab.arturbosch.detekt")
    id("maven-publish")
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
version = "2.0.0-RC1"

gradlePlugin {
    plugins.register("measure-builds") {
        id = "com.automattic.android.measure-builds"
        implementationClass = "com.automattic.android.measure.BuildTimePlugin"
    }
}

val awsAccessKey: String? by project
val awsSecretKey: String? by project
publishing {
    repositories {
        maven {
            url = uri("s3://a8c-libs.s3.amazonaws.com/android")
            credentials(AwsCredentials::class) {
                accessKey = awsAccessKey ?: System.getenv("AWS_ACCESS_KEY")
                secretKey = awsSecretKey ?: System.getenv("AWS_SECRET_KEY")
            }
        }
    }
}
