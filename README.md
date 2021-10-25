# Tracks Gradle

 [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22) 

Gradle Plugin for reporting build time results into Tracks system.

## How to use 

Configure plugin in `build.gradle` file:

```kotlin
plugins {
    id("io.github.wzieba.tracks.plugin")
}

(...)

tracks {
    automatticProject.set(io.github.wzieba.tracks.plugin.TracksExtension.AutomatticProject.WooCommerce)
    
    // Optional config
    customEventName.set("test_gradle_plugin") // Custom event name to be sent
    debug.set(true) // Enable additional logs
    uploadEnabled.set(true) // Opt-out from sending reports
}
```

After each build you should see

```
✅ Build time report of Xm Ys has been received by Tracks.
```

which confirms received report.