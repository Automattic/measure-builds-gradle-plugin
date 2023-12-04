package com.automattic.android.measure.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupedAppsMetrics(
    @SerialName("meta")
    val meta: List<AppsMetric>,
    @SerialName("metrics")
    val metrics: List<AppsMetric>,
)

@Serializable
data class AppsMetric(
    @SerialName("name")
    val name: String,
    @SerialName("value")
    val value: String,
)
