package com.automattic.android.measure.providers

import org.gradle.api.Project

object AuthTokenProvider {

    fun provide(project: Project): String? {
        return project.properties["appsMetricsToken"] as String?
    }
}
