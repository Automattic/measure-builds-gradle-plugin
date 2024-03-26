package com.automattic.android.measure

import com.automattic.android.measure.lifecycle.BuildFinishedFlowAction
import com.automattic.android.measure.lifecycle.BuildTaskService
import com.automattic.android.measure.lifecycle.ConfigurationPhaseObserver
import com.automattic.android.measure.providers.BuildDataProvider
import com.automattic.android.measure.providers.UsernameProvider
import com.automattic.android.measure.reporters.InMemoryMetricsReporter
import com.gradle.scan.plugin.BuildScanExtension
import kotlinx.coroutines.runBlocking
import org.gradle.StartParameter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.flow.FlowActionSpec
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.invocation.DefaultGradle
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@Suppress("UnstableApiUsage")
@ExperimentalTime
class BuildTimePlugin @Inject constructor(
    private val registry: BuildEventsListenerRegistry,
    private val flowScope: FlowScope,
    private val flowProviders: FlowProviders,
) : Plugin<Project> {
    override fun apply(project: Project) {
        val buildInitiatedTime =
            (project.gradle as DefaultGradle).services[BuildStartedTime::class.java].startTime
        val extension =
            project.extensions.create("measureBuilds", MeasureBuildsExtension::class.java, project)

        val metricsDispatcher = InMemoryMetricsReporter
        metricsDispatcher.buildMetricsPreparedAction = extension.buildMetricsPreparedAction

        project.afterEvaluate {
            if (extension.enable.convention(false).get() == true) {
                val configurationProvider: Provider<Boolean> = project.providers.of(
                    ConfigurationPhaseObserver::class.java
                ) { }
                ConfigurationPhaseObserver.init()

                val encodedUser: String = UsernameProvider.provide(project, extension)
                prepareBuildData(project, encodedUser)
                prepareBuildFinishedAction(
                    project.gradle.startParameter,
                    extension,
                    buildInitiatedTime,
                    configurationProvider
                )
            }
        }

        prepareBuildTaskService(project)
        prepareBuildScanListener(project, extension, metricsDispatcher)
    }

    private fun prepareBuildData(
        project: Project,
        encodedUser: String,
    ) {
        InMemoryReport.setBuildData(
            BuildDataProvider.provide(
                project,
                encodedUser,
            )
        )
    }

    private fun prepareBuildScanListener(
        project: Project,
        extension: MeasureBuildsExtension,
        analyticsReporter: InMemoryMetricsReporter,
    ) {
        val buildScanExtension = project.extensions.findByType(BuildScanExtension::class.java)
        val extensionEnable = extension.enable
        val attachGradleScanId = extension.attachGradleScanId
        buildScanExtension?.buildScanPublished {
            runBlocking {
                if (extensionEnable.get() == true && attachGradleScanId.get()) {
                    analyticsReporter.report(InMemoryReport, it.buildScanId)
                }
            }
        }
    }

    private fun prepareBuildFinishedAction(
        startParameter: StartParameter,
        extension: MeasureBuildsExtension,
        buildInitiatedTime: Long,
        configurationPhaseObserver: Provider<Boolean>,
    ) {
        flowScope.always(
            BuildFinishedFlowAction::class.java
        ) { spec: FlowActionSpec<BuildFinishedFlowAction.Parameters> ->
            spec.parameters.apply {
                this.buildWorkResult.set(flowProviders.buildWorkResult)
                this.attachGradleScanId.set(extension.attachGradleScanId)
                this.initiationTime.set(buildInitiatedTime)
                this.configurationPhaseExecuted.set(configurationPhaseObserver)
                this.startParameter.set(startParameter)
            }
        }
    }

    private fun prepareBuildTaskService(project: Project) {
        val serviceProvider: Provider<BuildTaskService> =
            project.gradle.sharedServices.registerIfAbsent(
                "taskEvents",
                BuildTaskService::class.java
            ) {
            }
        registry.onTaskCompletion(serviceProvider)
    }
}
