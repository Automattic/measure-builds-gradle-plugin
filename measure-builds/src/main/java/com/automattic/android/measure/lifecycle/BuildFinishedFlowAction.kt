@file:Suppress("UnstableApiUsage")

package com.automattic.android.measure.lifecycle

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.models.ExecutionData
import com.automattic.android.measure.reporters.InMemoryMetricsReporter
import org.gradle.StartParameter
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import kotlin.jvm.optionals.getOrNull

class BuildFinishedFlowAction : FlowAction<BuildFinishedFlowAction.Parameters> {
    interface Parameters : FlowParameters {

        // This value will NOT update if project re-used configuration cache
        // Use ONLY for calculating configuration phase duration if it was executed
        @get:Input
        val initiationTime: Property<Long>

        @get:Input
        val configurationPhaseExecuted: Property<Provider<Boolean>>

        @get:Input
        val buildWorkResult: Property<Provider<BuildWorkResult>>

        @get:ServiceReference
        val buildTaskService: Property<BuildTaskService>

        @get:Input
        val startParameter: Property<StartParameter>
    }

    override fun execute(parameters: Parameters) {
        val init = parameters.initiationTime.get()
        val buildStart = parameters.buildTaskService.get().buildStartTime
        val finish = System.currentTimeMillis()

        val result = parameters.buildWorkResult.get().get()
        val buildPhaseDuration = finish - buildStart

        val configurationTime = if (parameters.configurationPhaseExecuted.get().get()) {
            buildStart - init
        } else {
            0
        }

        val executionData = ExecutionData(
            buildTime = buildPhaseDuration + configurationTime,
            failed = result.failure.isPresent,
            failure = result.failure.getOrNull()?.message,
            executedTasks = parameters.buildTaskService.get().tasks,
            buildFinishedTimestamp = finish,
            configurationPhaseDuration = configurationTime,
            requestedTasks = parameters.startParameter.get().taskNames.toList()
        )

        InMemoryReport.setExecutionData(executionData)
        InMemoryMetricsReporter.report(InMemoryReport)
    }
}
