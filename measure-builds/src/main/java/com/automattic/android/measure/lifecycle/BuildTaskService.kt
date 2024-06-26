package com.automattic.android.measure.lifecycle

import com.automattic.android.measure.models.MeasuredTask
import com.automattic.android.measure.models.MeasuredTask.State.EXECUTED
import com.automattic.android.measure.models.MeasuredTask.State.IS_FROM_CACHE
import com.automattic.android.measure.models.MeasuredTask.State.UP_TO_DATE
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

    private val measuredTasks = mutableListOf<MeasuredTask>()

    val tasks: List<MeasuredTask>
        get() = measuredTasks

    val buildStartTime: Long = System.currentTimeMillis()

    override fun onFinish(event: FinishEvent?) {
        if (event is TaskFinishEvent) {
            if (event.result is SuccessResult) {
                val result = event.result as TaskSuccessResult

                measuredTasks.add(
                    MeasuredTask(
                        name = event.descriptor?.name.toString(),
                        duration = (event.result.endTime - event.result.startTime).milliseconds,
                        startTimestamp = event.result.startTime,
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
