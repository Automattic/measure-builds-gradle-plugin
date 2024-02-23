package com.automattic.android.measure

import com.automattic.android.measure.models.BuildData
import com.automattic.android.measure.models.ExecutionData
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
        // given
        val runner = runner()

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
    fun `given a project utilizes configuration cache, when build finishes, then assert that build or execution data was not reused`() {
        fun runTaskAndGetDetails(vararg arguments: String): Pair<List<String>, Long> {
            runner(*arguments).build()
            return buildData.tasks to executionData.buildFinishedTimestamp
        }

        val (tasksA, timestampA) = runTaskAndGetDetails("help")
        val (tasksB, timestampB) = runTaskAndGetDetails("outgoingVariants")
        val (tasksC, timestampC) = runTaskAndGetDetails("tasks")

        assertThat(timestampA).isNotEqualTo(timestampB)
        assertThat(timestampA).isNotEqualTo(timestampC)
        assertThat(timestampB).isNotEqualTo(timestampC)

        assertThat(tasksA).isNotEqualTo(tasksB)
        assertThat(tasksA).isNotEqualTo(tasksC)
        assertThat(tasksB).isNotEqualTo(tasksC)
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
//    @Disabled(
//        "This test is the reason, why we have to be extra careful with the configuration cache. Build data won't always be invalidated." +
//            "This means that we can have a situation where the build data is, reused what is not expected. "
//    )
    fun `given a project utilizes configuration cache, when build finishes twice with the same task, then assert that build data was not reused`() {
        runner("help", "-Dorg.gradle.workers.max=3").build()
        runner("help", "-Dorg.gradle.workers.max=5").build()
        val maxWorkers = buildData.maxWorkers

        assertThat(maxWorkers).isEqualTo(5)
    }

    private fun runner(vararg arguments: String): GradleRunner {
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()

        projectDir.resolve("settings.gradle.kts").writeText("")

        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.automattic.android.measure-builds")
            }
            measureBuilds {
                enable.set(true)
                attachGradleScanId.set(false)
                automatticProject.set(com.automattic.android.measure.MeasureBuildsExtension.AutomatticProject.WooCommerce)
            }
            """.trimIndent()
        )

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(*arguments, "--configuration-cache", "--stacktrace")
        runner.withProjectDir(projectDir)
        return runner
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
