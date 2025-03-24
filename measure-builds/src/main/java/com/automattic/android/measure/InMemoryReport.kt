package com.automattic.android.measure

import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.ExecutionData
import com.automattic.android.measure.models.RemoteBuildCacheData

object InMemoryReport {
    private var buildDataStore: BuildData? = null
    private var executionDataStore: ExecutionData? = null

    fun setBuildData(buildData: BuildData) {
        buildDataStore = buildData
    }

    fun setExecutionData(executionData: ExecutionData) {
        executionDataStore = executionData
    }

    val buildData: BuildData
        get() = buildDataStore ?: throw NullPointerException("Build data must not be null")

    val executionData: ExecutionData
        get() = executionDataStore ?: throw NullPointerException("Execution data must not be null")

    var remoteBuildCacheData: RemoteBuildCacheData = RemoteBuildCacheData()
}
