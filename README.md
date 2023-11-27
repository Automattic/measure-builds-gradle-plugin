# Measure-build-times Gradle Plugin [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22)

Gradle Plugin for reporting build time results into internal Automattic systems.

## Setup

### Directly in project

Configure plugin in `build.gradle` file:

```groovy
// settings.gradle
plugins {
    id "com.gradle.enterprise" version "latest_version" // optional
}

// build.gradle
plugins {
    id "io.github.wzieba.tracks.plugin" version "latest_tag"
}

tracks {
    automatticProject.set(io.github.wzieba.tracks.plugin.TracksExtension.AutomatticProject.WooCommerce)
    attachGradleScanId.set(true)
    // `false`, if no Enterprise plugin applied OR don't want to attach build scan id 
}
```

## Configuration

| Property           | Required? | Description                                                                                                                                                       |
|--------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| automatticProject  | yes       | Project that will determine event name                                                                                                                            |
| attachGradleScanId | yes       | Upload metrics after build scan is published, with build scan id attached. If `false`, metrics will be uploaded upon build finish, without build scan id attached |
| enabled            | no        | Enable plugin (def: `false`)                                                                                                                                      |
| obfuscateUsername  | no        | Obfuscate system username with SHA-1 (def: `false`)                                                                                                               | 

## Result

After each build you should see

```
✅ Build time report of 4m 8s has been received by Apps Metrics.
```

which confirms received report.

## Discrepancies between Gradle reports and this plugin

This plugin might report different data to what Gradle logs at the end of a build.

### Number of tasks

The reason is that Gradle log filters
out [lifecycle tasks](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:lifecycle_tasks),
while this plugin, cannot do this as it uses configuration-cache
compatible `OperationCompletionListener`.

### Build time

Gradle uses `BuildStartedTime` internal service to define where the build starts and ends. Those
moments are slightly different comparing to this plugin definitions. The discrepancy is minor, a few
seconds at most and still shows how long user waited for build to finish.
