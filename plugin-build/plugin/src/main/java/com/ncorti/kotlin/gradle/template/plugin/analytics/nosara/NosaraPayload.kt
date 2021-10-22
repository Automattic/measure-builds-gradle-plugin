package com.ncorti.kotlin.gradle.template.plugin.analytics.nosara

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NosaraPayload(
    @SerialName("events")
    val events: List<Event>,
)