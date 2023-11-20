# Tracks Gradle

 [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22) 

Gradle Plugin for reporting build time results into Tracks system.

Inspired [by work](https://github.com/jraska/github-client/tree/master/plugins/src/main/java/com/jraska/gradle/buildtime) of [@jraska](https://github.com/jraska). 

## How to use 

### Directly in project

Configure plugin in `build.gradle` file:

```groovy
plugins {
    id "io.github.wzieba.tracks.plugin" version "latest_tag"
}

tracks {
    automatticProject.set(io.github.wzieba.tracks.plugin.TracksExtension.AutomatticProject.WooCommerce)
}
```

## Configuration
| Property | Default | Required? | Description |
| --- | -- |-----------| --- |
| automatticProject | null | yes       | Project that will determine event name |
| enabled | null | no        | Enable plugin |
| obfuscateUsername | false | no | If true, then username will be SHA-1 obfuscated | 

## Result

After each build you should see

```
✅ Build time report of 4m 8s has been received by Apps Metrics.
```

which confirms received report.

## Discrepancies between Gradle reports and this plugin

This plugin might report different data to what Gradle logs at the end of a build.

### Number of tasks

The reason is that Gradle log filters out [lifecycle tasks](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:lifecycle_tasks), while this plugin, cannot do this as it uses configuration-cache compatible `OperationCompletionListener`.

### Build time

This plugin uses `BuildStartedTime` service, which is precisely what Gradle uses to log build duration at the end of the build.
The difference is where end of the build is defined - in this plugin, with `FlowAction`. The difference is minor and safe to ignore. 
