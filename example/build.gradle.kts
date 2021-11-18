plugins {
    java
    id("io.github.wzieba.tracks.plugin")
}

tracks {
    automatticProject.set(io.github.wzieba.tracks.plugin.TracksExtension.AutomatticProject.WooCommerce)
    enabled.set(true)
    customEventName.set("test_gradle_plugin")
}
