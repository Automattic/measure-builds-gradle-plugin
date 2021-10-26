# Tracks Gradle

 [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22) 

Gradle Plugin for reporting build time results into Tracks system.

## How to use 

### Directly in project

Configure plugin in `build.gradle` file:

```groovy
plugins {
    id "io.github.wzieba.tracks.plugin" version "0.0.2"
}

tracks {
    automatticProject = io.github.wzieba.tracks.plugin.TracksExtension.AutomatticProject.WooCommerce
    debug.set(true)
}
```

### Gradle Initialization Script

If you don't want to edit project files, you can use [initialization script](https://docs.gradle.org/current/userguide/init_scripts.html), e.g.

```groovy
initscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath "io.github.wzieba.tracks:io.github.wzieba.tracks.plugin:0.0.2"
    }
}

rootProject {
    if (name == "woocommerce-android") {
        apply plugin: io.github.wzieba.tracks.plugin.BuildTimePlugin

        tracks {
            automatticProject.set(io.github.wzieba.tracks.plugin.TracksExtension.AutomatticProject.WooCommerce)
            username.set("wzieba")
        }
    }
}
```

## Configuration
| Property | Default | Optional? | Description |
| --- | --- | --- | --- |
| automatticProject | null | false | Project that will determine event name
| username | "anon" | true | Username associated with report |
| uploadEnabled | true | true | Opt-out flag for sending reports to Tracks |
| customEventName | null | true | Event name that overrides one set by `automatticProject`, should be used for debug purposes. |
| debug | false | true | Show additional logs


## Result

After each build you should see

```
✅ Build time report of Xm Ys has been received by Tracks.
```

which confirms received report.