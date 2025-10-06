package com.automattic.android.measure

import com.automattic.android.measure.providers.MachineDataProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class MachineDataProviderTest {

    @Test
    fun testArchitectureLookup() {
        if (System.getenv("CI") != null) {
            assertEquals("amd64", MachineDataProvider().architecture())
        } else {
            assertEquals("aarch64", MachineDataProvider().architecture())
        }
    }
}
