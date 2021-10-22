package com.ncorti.kotlin.gradle.template.plugin

import org.codehaus.groovy.runtime.ProcessGroovyMethods
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
    fun buildData(result: BuildResult, statistics: TaskExecutionStatistics): BuildData {
        val start = nowMillis()

        val gradle = result.gradle as DefaultGradle
        val services = gradle.services

        val startTime = services[BuildStartedTime::class.java].startTime
        val totalTime = services[Clock::class.java].currentTime - startTime

        val daemonInfo = services[DaemonScanInfo::class.java]
        val startParameter = gradle.startParameter

        return BuildData(
            action = result.action,
            buildTime = totalTime,
            failed = result.failure != null,
            failure = result.failure,
            daemonsRunning = daemonInfo.numberOfRunningDaemons,
            thisDaemonBuilds = daemonInfo.numberOfBuilds,
            hostname = hostname(),
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
            buildDataCollectionOverhead = nowMillis() - start
        )
    }

    private fun hostname(): String {
        val process = Runtime.getRuntime().exec("hostname")
        process.waitFor()
        return ProcessGroovyMethods.getText(process).trim()
    }

    private fun Gradle.environment(): Environment {
        return when {
            rootProject.hasProperty("android.injected.invoked.from.ide") -> Environment.IDE
            System.getenv("CI") != null -> Environment.CI
            else -> Environment.CMD
        }
    }

    private fun nowMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}
