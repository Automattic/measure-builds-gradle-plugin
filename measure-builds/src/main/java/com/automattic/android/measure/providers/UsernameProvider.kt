package com.automattic.android.measure.providers

import com.automattic.android.measure.MeasureBuildsExtension
import org.codehaus.groovy.runtime.EncodingGroovyMethods
import org.gradle.api.Project

object UsernameProvider {

    fun provide(project: Project, extension: MeasureBuildsExtension): String {
        val user = project.providers.systemProperty("user.name").get()

        val encodedUser: String = user.let {
            if (extension.obfuscateUsername.getOrElse(false) == true) {
                EncodingGroovyMethods.digest(it, "SHA-1")
            } else {
                it
            }
        }

        return encodedUser
    }
}
