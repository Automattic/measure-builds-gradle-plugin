# measure-builds Gradle Plugin [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22)

Gradle Plugin for reporting build metrics at the end of the build.

## Features
- Add own, custom reporters and send metrics to any system
- Configuration Cache compatibility
- Gradle Enterprise integration - optionally attach Gradle Scan Id to the metrics

## Setup

```groovy
// settings.gradle
plugins {
    id "com.gradle.enterprise" version "latest_version" // optional
}

// build.gradle
import com.automattic.android.measure.reporters.MetricsReport
import com.automattic.android.measure.reporters.SlowSlowTasksMetricsReporter

plugins {
    id "com.automattic.android.measure-builds" version "latest_tag"
}

measureBuilds {
    enable = true
    attachGradleScanId = true // `false`, if no Enterprise plugin applied OR don't want to attach build scan id
    onBuildMetricsReadyListener { MetricsReport metricsReport ->
        // Use ready reporters
        SlowSlowTasksMetricsReporter.report(metricsReport)
        
        // or add your own reporters here or use 
        MyCustomReporter.report(metricsReport)
    }
}
```

## Configuration

| Property                    | Default | Description                                                                                                                                                       |
|-----------------------------|----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enable                      | `false` | Enable plugin                                                                                                                                      |
| onBuildMetricsReadyListener |    | Callback to be called when build metrics are ready to be reported. Use this to add your own reporters. |
| attachGradleScanId          | `false` | Upload metrics after build scan is published, with build scan id attached. If `false`, metrics will be uploaded upon build finish, without build scan id attached |
| obfuscateUsername           | `false` | Obfuscate system username with SHA-1                                                                                                               | 

## Demo

<img width="776" alt="image" src="https://github.com/Automattic/measure-builds-gradle-plugin/assets/5845095/62525db1-73bf-4fa8-ad67-59ad0e213748">
