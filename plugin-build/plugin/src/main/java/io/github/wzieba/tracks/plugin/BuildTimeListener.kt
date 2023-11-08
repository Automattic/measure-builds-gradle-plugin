package io.github.wzieba.tracks.plugin

import io.github.wzieba.tracks.plugin.analytics.Emojis.WAITING_ICON
import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatisticsEventAdapter
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger

internal class BuildTimeListener(
    private val buildDataFactory: BuildDataFactory,
    private val buildReporter: BuildReporter,
    private val tracksExtension: TracksExtension,
    private val includedBuilds: Collection<IncludedBuild>,
    private val logger: Logger,
) : BuildListener {
    private val taskExecutionStatisticsEventAdapter = TaskExecutionStatisticsEventAdapter()

    override fun settingsEvaluated(gradle: Settings) = Unit
    override fun projectsLoaded(gradle: Gradle) = Unit
    override fun projectsEvaluated(gradle: Gradle) {
        if (tracksExtension.enabled.get()) {
            gradle.addListener(taskExecutionStatisticsEventAdapter)
        }
    }

    override fun buildFinished(result: BuildResult) {
        if (tracksExtension.enabled.get()) {
            val buildData = buildDataFactory.buildData(
                result,
                taskExecutionStatisticsEventAdapter.statistics,
                tracksExtension.automatticProject.get(),
                includedBuilds.map(IncludedBuild::getName)
            )
            val encodedUser: String? = System.getProperty("user.name").let {
                if (tracksExtension.obfuscateUsername.getOrElse(false) == true) {
                    EncodingGroovyMethods.digest(it, "SHA-1")
                } else {
                    it
                }
            }

            logger.lifecycle("\n$WAITING_ICON Reporting build data to Apps Metrics...")
            buildReporter.report(
                buildData,
                encodedUser.orEmpty()
            )
        }
    }
}
