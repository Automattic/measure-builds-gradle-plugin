package com.automattic.android.measure

import com.automattic.android.measure.providers.MachineDataProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class MachineDataProviderTest {

    @Test
    fun testArchitectureLookup() {
        val actual = MachineDataProvider().architecture()
        val expected = System.getProperty("os.arch")
        println("Architecture: $actual (expected: $expected)")
        assertEquals(expected, actual)
    }
}
