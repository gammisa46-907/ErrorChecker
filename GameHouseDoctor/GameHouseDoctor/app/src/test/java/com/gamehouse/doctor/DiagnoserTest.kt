package com.gamehouse.doctor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnoserTest {

    private val academy = Games.get("academy_of_magic")!!

    @Test
    fun fullReportHasAllSectionsAndFlashDetails() {
        val t = Diagnoser.gameReport(academy, null, null)
        listOf("PROFIL TEKNIS", "BASELINE", "HASIL PEMERIKSAAN FILE", "SOLUSI BERTAHAP", "PENGATURAN WINLATOR YANG DISARANKAN", "URUTAN PEMECAHAN MASALAH", "CATATAN KEJUJURAN")
            .forEach { assertTrue("hilang: $it", t.contains(it)) }
        assertTrue(t.contains("D27CDB6E-AE6D-11CF-96B8-444553540000"))
        assertTrue(t.contains("regsvr32"))
        assertTrue(t.contains("HKCU\\Software\\JEDI-VCL\\TipsStartup"))
    }

    @Test
    fun symptomReportFiltersToMatchingCauses() {
        val t = Diagnoser.gameReport(academy, null, Symptom.OLE_ERROR)
        assertTrue(t.contains("PENYEBAB YANG COCOK DENGAN GEJALA"))
        assertTrue(t.contains("Shockwave Flash"))
        assertTrue(t.contains("RISIKO LAIN UNTUK GAME INI"))
    }

    @Test
    fun scanProblemsAppearInReportAndRaiseIntegrity() {
        val m = GameCatalog.all.first { it.id == "adventure_inlay" }
        val items = m.files.filter { it.name != "levels.bin" }.map { ZipItem("${m.folder}/${it.name}", false, it.size, it.crc, false) }
        val scan = PackScanner.scan(items).forGame("adventure_inlay")
        val t = Diagnoser.gameReport(Games.get("adventure_inlay")!!, scan, null)
        assertTrue(t.contains("HILANG: levels.bin"))
        assertTrue(t.contains("Pemindaian ZIP menemukan perbedaan"))
    }

    @Test
    fun confirmedLogIssueIsMarkedAndListedFirst() {
        val t = Diagnoser.gameReport(academy, null, null, setOf("PACKED_EXE"))
        assertTrue(t.contains("TERKONFIRMASI DARI LOG"))
        assertTrue(t.indexOf("[1] EXE terbungkus") >= 0)
    }

    @Test
    fun logReportConfirmsFlashHypothesis() {
        val log = sequenceOf(
            "0150:err:module:whatever AcademyofMagic.exe started",
            "err:ole:CoGetClassObject class {D27CDB6E-AE6D-11CF-96B8-444553540000} not registered"
        )
        val t = Diagnoser.logReport(LogAnalyzer.analyze(log))
        assertTrue(t.contains("HIPOTESIS FLASH TERKONFIRMASI"))
        assertTrue(t.contains("Academy of Magic"))
        assertTrue(t.contains("SOLUSI BERTAHAP"))
    }

    @Test
    fun emptyLogGivesGuidanceNotAnError() {
        val t = Diagnoser.logReport(LogAnalyzer.analyze(sequenceOf("hello", "world")))
        assertTrue(t.contains("Tidak ada pola error yang dikenali"))
        assertFalse(t.contains("HIPOTESIS FLASH"))
    }

    @Test
    fun packSummaryCountsStatuses() {
        val all = GameCatalog.all.flatMap { m -> m.files.map { ZipItem("${m.folder}/${it.name}", false, it.size, it.crc, false) } }
        val t = Diagnoser.packSummary(PackScanner.scan(all))
        assertTrue(t.contains("RINGKASAN PEMINDAIAN ZIP"))
        assertTrue(t.contains("Game dikenali: 15 dari 15"))
    }
}
