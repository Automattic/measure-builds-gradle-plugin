package com.automattic.android.measure

import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.ExecutionData

object InMemoryReport : Report {
    var buildDataStore: BuildData? = null
    var executionDataStore: ExecutionData? = null

    override val buildData: BuildData
        get() = buildDataStore ?: throw NullPointerException("Must not be null")

    override val executionData: ExecutionData
        get() = executionDataStore ?: throw NullPointerException("Must not be null")
}

interface Report {
    val buildData: BuildData
    val executionData: ExecutionData
}
