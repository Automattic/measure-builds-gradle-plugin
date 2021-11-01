package io.github.wzieba.tracks.plugin

import org.gradle.BuildResult
import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatistics
import org.gradle.api.invocation.Gradle
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.internal.time.Clock
import org.gradle.invocation.DefaultGradle
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo
import java.util.concurrent.TimeUnit

object BuildDataFactory {

    @Suppress("UnstableApiUsage")
    fun buildData(
        result: BuildResult,
        statistics: TaskExecutionStatistics,
        automatticProject: TracksExtension.AutomatticProject,
        includedBuildsNames: List<String>,
    ): BuildData {
        val start = nowMillis()

        val gradle = result.gradle as DefaultGradle
        val services = gradle.services

        val startTime = services[BuildStartedTime::class.java].startTime
        val totalTime = services[Clock::class.java].currentTime - startTime

        val daemonInfo = services[DaemonScanInfo::class.java]
        val startParameter = gradle.startParameter

        return BuildData(
            forProject = automatticProject,
            action = result.action,
            buildTime = totalTime,
            failed = result.failure != null,
            failure = result.failure,
            daemonsRunning = daemonInfo.numberOfRunningDaemons,
            thisDaemonBuilds = daemonInfo.numberOfBuilds,
            tasks = startParameter.taskNames,
            environment = gradle.environment(),
            gradleVersion = gradle.gradleVersion,
            operatingSystem = System.getProperty("os.name").toLowerCase(),
            isConfigureOnDemand = startParameter.isConfigureOnDemand,
            isConfigurationCache = startParameter.isConfigurationCache,
            isBuildCache = startParameter.isBuildCacheEnabled,
            maxWorkers = startParameter.maxWorkerCount,
            taskStatistics = TaskStatistics(
                statistics.totalTaskCount,
                statistics.upToDateTaskCount,
                statistics.fromCacheTaskCount,
                statistics.executedTasksCount
            ),
            buildDataCollectionOverhead = nowMillis() - start,
            includedBuildsNames = includedBuildsNames,
            architecture = architecture(),
        )
    }

    private fun Gradle.environment(): Environment {
        return when {
            rootProject.hasProperty("android.injected.invoked.from.ide") -> Environment.IDE
            System.getenv("CI") != null -> Environment.CI
            else -> Environment.CMD
        }
    }

    private fun architecture(): String {
        val exec = Runtime.getRuntime().exec("uname -m")
        val inputStream = exec.inputStream
        exec.waitFor()
        return inputStream.bufferedReader().readText().trim()
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}
