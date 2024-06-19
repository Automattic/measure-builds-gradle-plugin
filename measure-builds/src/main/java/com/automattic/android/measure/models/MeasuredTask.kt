package com.automattic.android.measure.models

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class MeasuredTask(
    val name: String,
    val duration: Duration,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val state: State
) {
    enum class State {
        UP_TO_DATE,
        IS_FROM_CACHE,
        EXECUTED,
    }
}
