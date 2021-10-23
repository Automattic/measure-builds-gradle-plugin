object PluginCoordinates {
    const val ID = "com.automattic.kotlin.gradle.tracks.plugin"
    const val GROUP = "com.automattic.kotlin.gradle.tracks"
    const val VERSION = "0.0.1-SNAPSHOT"
    const val IMPLEMENTATION_CLASS = "com.automattic.kotlin.gradle.tracks.plugin.BuildTimePlugin"
}

object PluginBundle {
    const val VCS = "https://github.com/wzieba/tracks-gradle/"
    const val WEBSITE = "https://github.com/wzieba/tracks-gradle/"
    const val DESCRIPTION = "Gradle plugin which reports build times to Tracks."
    const val DISPLAY_NAME = "Gradle plugin which reports build times to Tracks."
    val TAGS = listOf(
        "plugin",
        "gradle",
        "automattic",
        "tracks"
    )
}

