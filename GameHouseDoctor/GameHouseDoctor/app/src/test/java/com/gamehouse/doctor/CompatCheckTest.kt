package com.gamehouse.doctor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatCheckTest {

    private fun byLabel(c: List<Check>, label: String) = c.first { it.label == label }

    @Test
    fun modernArm64DeviceIsOk() {
        val c = CompatCheck.evaluate(listOf("arm64-v8a", "armeabi-v7a"), 34, 0x00030002, (1 shl 22) or (3 shl 12))
        assertEquals(Status.OK, byLabel(c, "CPU ARM 64-bit").status)
        assertEquals(Status.OK, byLabel(c, "Versi Android").status)
        assertEquals(Status.OK, byLabel(c, "OpenGL ES").status)
        assertTrue(byLabel(c, "Vulkan").detail.startsWith("1.3.0"))
    }

    @Test
    fun x86AndOldAndroidFail() {
        val c = CompatCheck.evaluate(listOf("x86_64", "x86"), 25, 0x00010001, null)
        assertEquals(Status.FAIL, byLabel(c, "CPU ARM 64-bit").status)
        assertEquals(Status.FAIL, byLabel(c, "Versi Android").status)
        assertEquals(Status.FAIL, byLabel(c, "OpenGL ES").status)
        assertTrue(byLabel(c, "Vulkan").detail.contains("tidak terdeteksi"))
    }

    @Test
    fun only32BitArmFails() {
        val c = CompatCheck.evaluate(listOf("armeabi-v7a"), 30, 0x00030000, 0)
        assertEquals(Status.FAIL, byLabel(c, "CPU ARM 64-bit").status)
    }
}
