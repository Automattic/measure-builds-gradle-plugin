package com.automattic.kotlin.gradle.tracks.plugin.analytics.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracksPayload(
    @SerialName("events")
    val events: List<Event>,
)
