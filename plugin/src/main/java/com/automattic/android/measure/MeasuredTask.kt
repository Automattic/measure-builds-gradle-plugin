package com.automattic.android.measure

import kotlin.time.Duration

internal data class MeasuredTask(
    val name: String,
    val duration: Duration,
    val state: State
) {
    enum class State {
        UP_TO_DATE,
        IS_FROM_CACHE,
        EXECUTED,
    }
}
