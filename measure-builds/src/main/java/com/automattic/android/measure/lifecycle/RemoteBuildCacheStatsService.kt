package com.automattic.android.measure.lifecycle

import com.automattic.android.measure.InMemoryReport
import com.automattic.android.measure.models.OriginExecutionTaskData
import com.automattic.android.measure.models.RemoteBuildCacheData
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.caching.internal.controller.operations.LoadOperationDetails
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.internal.hash.HashCode
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent

abstract class RemoteBuildCacheStatsService : BuildService<BuildServiceParameters.None>,
    BuildOperationListener {

    init {
        InMemoryReport.remoteBuildCacheData = RemoteBuildCacheData()
    }

    override fun started(
        buildOperation: BuildOperationDescriptor,
        startEvent: OperationStartEvent,
    ) {
    }

    override fun progress(
        operationIdentifier: OperationIdentifier,
        progressEvent: OperationProgressEvent,
    ) {
    }

    override fun finished(
        buildOperation: BuildOperationDescriptor,
        finishEvent: OperationFinishEvent,
    ) {
        if (
            (finishEvent.result is BuildCacheRemoteLoadBuildOperationType.Result) &&
            (finishEvent.result as BuildCacheRemoteLoadBuildOperationType.Result).isHit
        ) {
            val details = buildOperation.details as LoadOperationDetails

            InMemoryReport.remoteBuildCacheData.remoteLoadTimes[details.cacheKey] =
                finishEvent.endTime - finishEvent.startTime
        }
        if (finishEvent.result is ExecuteTaskBuildOperationType.Result) {
            val result = finishEvent.result as ExecuteTaskBuildOperationType.Result

            result.takeIf { !it.isIncremental }?.let {
                result.originBuildCacheKeyBytes?.let { HashCode.fromBytes(it) }
                    ?.toString() to result.originExecutionTime
            }?.let { (cacheKey, executionTime) ->
                if (cacheKey != null && executionTime != null) {
                    InMemoryReport.remoteBuildCacheData.originExecutions[cacheKey] = OriginExecutionTaskData(
                        name = buildOperation.name,
                        executionTime = executionTime,
                    )
                }
            }
        }
    }
}