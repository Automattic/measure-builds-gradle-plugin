package io.github.wzieba.tracks.plugin

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.invocation.DefaultGradle
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo
import java.util.concurrent.TimeUnit

object BuildDataFactory {

    fun buildData(
        project: Project,
        automatticProject: TracksExtension.AutomatticProject,
        username: String,
    ): BuildData {
        val start = nowMillis()
        val gradle = project.gradle

        val services = (gradle as DefaultGradle).services

        val daemonInfo = services[DaemonScanInfo::class.java]
        val startParameter = gradle.startParameter

        @Suppress("UnstableApiUsage")
        return BuildData(
            forProject = automatticProject,
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
            buildDataCollectionOverhead = nowMillis() - start,
            includedBuildsNames = gradle.includedBuilds.toList().map { it.name },
            architecture = architecture(project),
            user = username
        )
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

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}
