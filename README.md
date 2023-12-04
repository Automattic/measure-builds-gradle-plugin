# measure-builds Gradle Plugin [![Pre Merge Checks](https://github.com/cortinico/kotlin-gradle-plugin-template/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/cortinico/kotlin-gradle-plugin-template/actions?query=workflow%3A%22Pre+Merge+Checks%22)

Gradle Plugin for reporting build metrics into internal Automattic systems.

## Setup

```groovy
// settings.gradle
plugins {
    id "com.gradle.enterprise" version "latest_version" // optional
}

// build.gradle
plugins {
    id "com.automattic.android.measure-builds" version "latest_tag"
}

measureBuilds {
    automatticProject.set(com.automattic.android.measure.MeasureBuildsExtension.AutomatticProject.WooCommerce)
    attachGradleScanId.set(true) // `false`, if no Enterprise plugin applied OR don't want to attach build scan id 
}
```

## Configuration

| Property           | Required? | Description                                                                                                                                                       |
|--------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| automatticProject  | yes       | Project that will determine event name                                                                                                                            |
| attachGradleScanId | yes       | Upload metrics after build scan is published, with build scan id attached. If `false`, metrics will be uploaded upon build finish, without build scan id attached |
| enable             | no        | Enable plugin (def: `false`)                                                                                                                                      |
| obfuscateUsername  | no        | Obfuscate system username with SHA-1 (def: `false`)                                                                                                               | 

## Demo

<img width="776" alt="image" src="https://github.com/Automattic/measure-builds-gradle-plugin/assets/5845095/62525db1-73bf-4fa8-ad67-59ad0e213748">
