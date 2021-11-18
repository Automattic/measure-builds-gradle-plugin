package io.github.wzieba.tracks.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class TracksExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val automatticProject: Property<AutomatticProject> = objects.property(AutomatticProject::class.java)

    val enabled: Property<Boolean> = objects.property(Boolean::class.java)

    @Optional
    val username: Property<String> = objects.property(String::class.java)

    @Optional
    val customEventName: Property<String> = objects.property(String::class.java)

    enum class AutomatticProject {
        WooCommerce, WordPress, DayOne
    }
}
