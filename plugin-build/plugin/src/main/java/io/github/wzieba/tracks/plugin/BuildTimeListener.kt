package io.github.wzieba.tracks.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatisticsEventAdapter
import org.gradle.api.invocation.Gradle

internal class BuildTimeListener(
    private val buildDataFactory: BuildDataFactory,
    private val buildReporter: BuildReporter,
    private val tracksExtension: TracksExtension
) : BuildListener {
    private val taskExecutionStatisticsEventAdapter = TaskExecutionStatisticsEventAdapter()

    override fun settingsEvaluated(gradle: Settings) = Unit
    override fun projectsLoaded(gradle: Gradle) = Unit
    override fun projectsEvaluated(gradle: Gradle) {
        gradle.addListener(taskExecutionStatisticsEventAdapter)
    }

    override fun buildFinished(result: BuildResult) {
        val buildData = buildDataFactory.buildData(
            result,
            taskExecutionStatisticsEventAdapter.statistics,
            tracksExtension.automatticProject.get()
        )

        if (tracksExtension.uploadEnabled.getOrElse(true)) {
            buildReporter.report(
                buildData,
                tracksExtension.username.getOrElse(ANONYMOUS_TRACKS_USER),
                tracksExtension.debug.getOrElse(false)
            )
        }
    }

    companion object {
        private const val ANONYMOUS_TRACKS_USER = "anon"
    }
}
