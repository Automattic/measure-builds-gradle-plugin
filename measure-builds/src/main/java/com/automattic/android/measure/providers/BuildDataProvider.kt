package com.automattic.android.measure.providers

import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.Environment
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

object BuildDataProvider {

    fun provide(
        project: Project,
        username: String,
    ): BuildData {
        val gradle = project.gradle
        val startParameter = gradle.startParameter

        val machineData = MachineDataProvider()

        @Suppress("UnstableApiUsage")
        return BuildData(
            environment = gradle.environment(),
            gradleVersion = gradle.gradleVersion,
            operatingSystem = machineData.operatingSystem(),
            isConfigurationCache = startParameter.isConfigurationCacheRequested,
            includedBuildsNames = gradle.includedBuilds.toList().map { it.name },
            architecture = machineData.architecture(),
            user = username,
        )
    }

    private fun Gradle.environment(): Environment {
        return when {
            rootProject.hasProperty("android.injected.invoked.from.ide") -> Environment.IDE
            System.getenv("CI") != null -> Environment.CI
            else -> Environment.CMD
        }
    }
}
