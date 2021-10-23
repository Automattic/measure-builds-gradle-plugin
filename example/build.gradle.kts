plugins {
    java
    id("com.automattic.kotlin.gradle.tracks.plugin")
}

tracks {
    automatticProject.set(com.automattic.kotlin.gradle.tracks.plugin.TracksExtension.AutomatticProject.TracksGradle)
    debug.set(false)
    uploadEnabled.set(true)
}
