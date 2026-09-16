package com.automattic.android.measure.providers

import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.Environment
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

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
            environment = environment(project.providers),
            gradleVersion = gradle.gradleVersion,
            operatingSystem = machineData.operatingSystem(),
            isConfigurationCache = startParameter.isConfigurationCacheRequested,
            includedBuildsNames = gradle.includedBuilds.toList().map { it.name },
            architecture = machineData.architecture(),
            user = username,
        )
    }

    private fun environment(providers: ProviderFactory): Environment {
        return when {
            providers.gradleProperty("android.injected.invoked.from.ide").isPresent -> Environment.IDE
            System.getenv("CI") != null -> Environment.CI
            else -> Environment.CMD
        }
    }
}
