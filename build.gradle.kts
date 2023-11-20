tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

tasks.register("preMerge") {
    description = "Runs all the verification tasks."

    dependsOn(":plugin:check")
    dependsOn(":plugin:validatePlugins")
}
