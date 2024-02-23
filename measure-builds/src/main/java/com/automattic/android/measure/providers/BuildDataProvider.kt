package com.automattic.android.measure.providers

import com.automattic.android.measure.MeasureBuildsExtension
import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.Environment
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.invocation.DefaultGradle
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo

object BuildDataProvider {
    fun provide(
        project: Project,
        automatticProject: Provider<MeasureBuildsExtension.AutomatticProject>,
        username: String,
    ): BuildData {
        val gradle = project.gradle

        val services = (gradle as DefaultGradle).services

        val daemonInfo = services[DaemonScanInfo::class.java]
        val startParameter = gradle.startParameter

        @Suppress("UnstableApiUsage")
        return BuildData(
            daemonsRunning = daemonInfo.numberOfRunningDaemons,
            thisDaemonBuilds = daemonInfo.numberOfBuilds,
            tasks = startParameter.taskNames,
            environment = gradle.environment(),
            gradleVersion = gradle.gradleVersion,
            operatingSystem = System.getProperty("os.name").lowercase(),
            isConfigureOnDemand = startParameter.isConfigureOnDemand,
            isConfigurationCache = startParameter.isConfigurationCacheRequested,
            isBuildCache = startParameter.isBuildCacheEnabled,
            maxWorkers = startParameter.maxWorkerCount,
            includedBuildsNames = gradle.includedBuilds.toList().map { it.name },
            architecture = architecture(project),
            user = username,
        ).apply {
            projectProvider = automatticProject
        }
    }

    private fun Gradle.environment(): Environment {
        return when {
            rootProject.hasProperty("android.injected.invoked.from.ide") -> Environment.IDE
            System.getenv("CI") != null -> Environment.CI
            else -> Environment.CMD
        }
    }

    private fun architecture(project: Project): String {
        val exec = project.providers.exec {
            it.commandLine("uname", "-m")
        }.standardOutput.asText.get()
        return exec.trim()
    }
}
