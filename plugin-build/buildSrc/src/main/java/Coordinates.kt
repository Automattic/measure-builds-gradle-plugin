object PluginCoordinates {
    const val ID = "io.github.wzieba.tracks.plugin"
    const val GROUP = "io.github.wzieba.tracks"
    const val VERSION = "1.0.0"
    const val IMPLEMENTATION_CLASS = "io.github.wzieba.tracks.plugin.BuildTimePlugin"
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

