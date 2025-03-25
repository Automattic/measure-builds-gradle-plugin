package com.automattic.android.measure

import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.Environment
import com.automattic.android.measure.models.ExecutionData
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Test suite focused on validating the behavior of the Build Time Plugin when the configuration cache is enabled.
 */
class BuildTimePluginConfigurationCacheTests {

    @BeforeEach
    fun clear() {
        File("build/functionalTest").listFiles()?.forEach {
            if (it.name != "measure-builds") {
                it.deleteRecursively()
            }
        }
        File(".gradle/configuration-cache").deleteRecursively()
    }

    @Test
    fun `given a project utilizes configuration cache, when build finishes, then report 0 configuration cache duration`() {
        // when
        runner("help", "--stacktrace").build()

        // then
        assertThat(executionData.configurationPhaseDuration).isGreaterThan(0)

        // when
        runner("help").build()

        // then
        assertThat(executionData.configurationPhaseDuration).isEqualTo(0)
    }

    @Test
    fun `given a project utilizes configuration cache, when build finishes, then assert that execution data was not reused`() {
        fun runTaskAndGetDetails(vararg arguments: String): Long {
            runner(*arguments).build()
            return executionData.buildFinishedTimestamp
        }

        val timestampA = runTaskAndGetDetails("help")
        val timestampB = runTaskAndGetDetails("outgoingVariants")
        val timestampC = runTaskAndGetDetails("tasks")

        assertThat(timestampA).isNotEqualTo(timestampB)
        assertThat(timestampA).isNotEqualTo(timestampC)
        assertThat(timestampB).isNotEqualTo(timestampC)
    }

    @Test
    fun `given a project utilizes configuration cache, when build finishes twice with the same task, then assert that execution data was not reused`() {
        runner("help").build()
        val timestampA = executionData.buildFinishedTimestamp
        runner("help").build()
        val timestampB = executionData.buildFinishedTimestamp

        assertThat(timestampA).isNotEqualTo(timestampB)
    }

    @Test
    fun `verify if environment change invalidates CC cache`() {
        runner("help").build()
        assertThat(buildData.environment).isNotEqualTo(Environment.IDE)

        runner("help", "-Pandroid.injected.invoked.from.ide=true").build()
        assertThat(buildData.environment).isEqualTo(Environment.IDE)
    }

    @Test
    fun `given a task which configuration is cached, when calling it again, then the requested task is correct`() {
        runner("help").build()
        assertThat(executionData.requestedTasks).contains("help")

        runner("outgoingVariants").build()
        assertThat(executionData.requestedTasks).contains("outgoingVariants")

        runner("help").build()
        assertThat(executionData.requestedTasks).contains("help")
    }

    private fun runner(vararg arguments: String): GradleRunner {
        val projectDir = File("build/functionalTest").apply {
            mkdirs()
            resolve("settings.gradle.kts").writeText("")
            resolve("build.gradle.kts").writeText(
                """
            plugins {
                id("com.automattic.android.measure-builds")
            }
            val buildPathProperty = project.layout.buildDirectory.map { it.asFile.path }
            measureBuilds {
                enable.set(true)
                onBuildMetricsReadyListener {
                    val buildPath = buildPathProperty.get()
                    com.automattic.android.measure.reporters.LocalMetricsReporter.report(this, buildPath)
                    com.automattic.android.measure.reporters.SlowSlowTasksMetricsReporter.report(this)
                }
            }
                """.trimIndent()
            )
        }

        return GradleRunner.create().forwardOutput().withPluginClasspath()
            .withArguments(*arguments, "--configuration-cache").withProjectDir(projectDir)
    }

    private val executionData: ExecutionData
        get() {
            File("build/functionalTest/build/reports/measure_builds/execution_data.json").let {
                return Json.decodeFromString<ExecutionData>(it.readText())
            }
        }

    private val buildData: BuildData
        get() {
            File("build/functionalTest/build/reports/measure_builds/build_data.json").let {
                return Json.decodeFromString<BuildData>(it.readText())
            }
        }
}
