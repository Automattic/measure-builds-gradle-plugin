package com.automattic.android.measure

import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatistics
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.SuccessResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskSuccessResult

abstract class BuildTaskService : BuildService<BuildServiceParameters.None>, OperationCompletionListener {

    private var upToDate = 0
    private var fromCache = 0
    private var executed = 0

    val taskStatistics
        get() = TaskExecutionStatistics(executed, fromCache, upToDate)

    override fun onFinish(event: FinishEvent?) {
        if (event is TaskFinishEvent) {
            if (event.result is SuccessResult) {
                val result = event.result as TaskSuccessResult
                when {
                    result.isFromCache -> fromCache++
                    result.isUpToDate -> upToDate++
                    else -> executed++
                }
            }
        }
    }
}
