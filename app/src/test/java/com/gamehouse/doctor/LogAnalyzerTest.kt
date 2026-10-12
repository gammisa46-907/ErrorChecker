package com.gamehouse.doctor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogAnalyzerTest {

    private fun run(vararg lines: String) = LogAnalyzer.analyze(lines.asSequence())

    @Test
    fun flashClassMapsToFlashIssue() {
        val a = run("fixme:ole:CoCreateInstanceEx no instance created for interface {00000112-0000-0000-c000-000000000046} of class {D27CDB6E-AE6D-11CF-96B8-444553540000}, hres is 0x80040154")
        assertEquals(1, a.findings.size)
        assertEquals("FLASH_ACTIVEX", a.findings[0].issueId)
        assertTrue(a.findings[0].title.contains("Flash"))
    }

    @Test
    fun stockRegisteredClassesGetRegistryAdviceWithoutIssue() {
        val a = run("err:ole:CoGetClassObject class {636b9f10-0c7d-11d1-95b2-0020afdc7421} not registered")
        assertTrue(a.findings[0].title.contains("DirectMusic"))
        assertNull(a.findings[0].issueId)
        assertTrue(a.findings[0].advice.contains("TERDAFTAR"))
    }

    @Test
    fun ieClassMapsToHtmlControl() {
        val a = run("err:ole:CoGetClassObject class {8856F961-340A-11D0-A96B-00C04FD705A2} not registered")
        assertEquals("HTML_CONTROL", a.findings[0].issueId)
    }

    @Test
    fun unknownGuidIsReportedVerbatim() {
        val a = run("err:ole:CoGetClassObject class {12345678-1234-1234-1234-123456789abc} not registered")
        assertTrue(a.findings[0].title.contains("{12345678-1234-1234-1234-123456789abc}"))
    }

    @Test
    fun packDllMapsToLoaderIssue() {
        val a = run("err:module:import_dll Library levels.bin (which is needed by L\"C:\\\\x\\\\AdventureInlay.exe\") not found")
        assertEquals("LOADER_DLL", a.findings[0].issueId)
        assertTrue(a.gamesDetected.contains("adventure_inlay"))
    }

    @Test
    fun windowsComponentDllPointsToWinComponents() {
        val a = run("err:module:import_dll Library MSVCR80.dll (which is needed by L\"C:\\\\game.exe\") not found")
        assertTrue(a.findings[0].advice.contains("vcrun2005"))
    }

    @Test
    fun wineSubsystemErrorsMapToIssues() {
        val a = run("err:ddraw:something failed", "err:d3d:device failed", "err:dsound:no device", "Unhandled exception: page fault")
        val ids = a.findings.mapNotNull { it.issueId }.toSet()
        assertEquals(setOf("DDRAW_DISPLAY", "D3D8_RENDER", "AUDIO_WAVEOUT", "PACKED_EXE"), ids)
    }

    @Test
    fun repeatedLinesMergeWithCountAndGamesAreDetected() {
        val l = "err:ole:CoGetClassObject class {D27CDB6E-AE6D-11CF-96B8-444553540000} not registered"
        val a = run("start Zuma.exe", "C:\\\\Hamsterball.exe", l, l)
        assertEquals(1, a.findings.size)
        assertTrue(a.findings[0].evidence.contains("2 kali"))
        assertEquals(setOf("hamsterball"), a.gamesDetected.toSet())
    }
}
