package com.automattic.android.measure

import com.automattic.android.measure.MeasuredTask.State.EXECUTED
import com.automattic.android.measure.MeasuredTask.State.IS_FROM_CACHE
import com.automattic.android.measure.MeasuredTask.State.UP_TO_DATE
import org.gradle.api.internal.tasks.execution.statistics.TaskExecutionStatistics
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.SuccessResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskSuccessResult
import kotlin.time.Duration.Companion.milliseconds

abstract class BuildTaskService :
    BuildService<BuildServiceParameters.None>,
    OperationCompletionListener {
    private val tasks = mutableListOf<MeasuredTask>()

    val taskStatistics: TaskExecutionStatistics
        get() {
            val groups = tasks.groupBy { it.state }

            return TaskExecutionStatistics(
                groups[EXECUTED].sizeOrZero(),
                groups[IS_FROM_CACHE].sizeOrZero(),
                groups[UP_TO_DATE].sizeOrZero()
            )
        }

    private fun <K> Collection<K>?.sizeOrZero() = this?.size ?: 0

    override fun onFinish(event: FinishEvent?) {
        if (event is TaskFinishEvent) {
            if (event.result is SuccessResult) {
                val result = event.result as TaskSuccessResult

                tasks.add(
                    MeasuredTask(
                        name = event.displayName,
                        duration = (event.result.endTime - event.result.startTime).milliseconds,
                        state = when {
                            result.isFromCache -> IS_FROM_CACHE
                            result.isUpToDate -> UP_TO_DATE
                            else -> EXECUTED
                        }
                    )
                )
            }
        }
    }
}
