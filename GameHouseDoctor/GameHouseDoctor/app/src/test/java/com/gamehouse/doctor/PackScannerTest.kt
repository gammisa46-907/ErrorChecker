package com.gamehouse.doctor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackScannerTest {

    /** Daftar isi ZIP yang sempurna, disusun dari katalog referensi. */
    private fun perfect(prefix: String = ""): List<ZipItem> {
        val out = ArrayList<ZipItem>()
        for (m in GameCatalog.all) {
            for (f in m.files) out.add(ZipItem("$prefix${m.folder}/${f.name}", false, f.size, f.crc, false))
            for (d in m.dirs) for (i in 0 until d.files) out.add(ZipItem("$prefix${m.folder}/${d.name}/f$i.bin", false, 10L, 0L, false))
        }
        return out
    }

    private fun status(scan: PackScan, id: String) = scan.forGame(id)!!.status

    @Test
    fun perfectPackIsAllComplete() {
        val s = PackScanner.scan(perfect())
        assertEquals(15, s.games.size)
        assertTrue(s.games.all { it.status == GameStatus.LENGKAP })
    }

    @Test
    fun packWrappedInExtraFolderIsStillRecognized() {
        val s = PackScanner.scan(perfect("_GameHouse_Games_Collection/"))
        assertTrue(s.games.all { it.status == GameStatus.LENGKAP })
    }

    @Test
    fun missingCriticalFileIsReported() {
        val s = PackScanner.scan(perfect().filter { !it.path.endsWith("levels.bin") })
        val g = s.forGame("adventure_inlay")!!
        assertEquals(GameStatus.PERLU_PERHATIAN, g.status)
        assertTrue(g.missing.contains("levels.bin"))
        assertEquals(GameStatus.LENGKAP, status(s, "hamsterball"))
    }

    @Test
    fun sizeAndCrcDifferencesAreReported() {
        val items = perfect().map {
            when {
                it.path.endsWith("Hamsterball/resball.dll") -> it.copy(size = it.size + 1)
                it.path.endsWith("Tap a Jam/Vorbis.dll") -> it.copy(crc = it.crc xor 0xFF)
                else -> it
            }
        }
        val s = PackScanner.scan(items)
        assertEquals("resball.dll", s.forGame("hamsterball")!!.sizeDiffs[0].name)
        assertEquals(listOf("Vorbis.dll"), s.forGame("tap_a_jam")!!.crcDiffs)
    }

    @Test
    fun volatileFilesOnlyNeedToExist() {
        val items = perfect().map { if (it.path.endsWith("Install.log")) it.copy(size = 9999L, crc = 1L) else it }
        assertTrue(PackScanner.scan(items).games.all { it.status == GameStatus.LENGKAP })
    }

    @Test
    fun missingFolderAndFileCountAreReported() {
        val noTextures = PackScanner.scan(perfect().filter { !it.path.contains("Hamsterball/Textures/") })
        assertTrue(noTextures.forGame("hamsterball")!!.missingDirs.contains("Textures"))
        val oneXtraLess = PackScanner.scan(perfect().filter { !it.path.startsWith("Inspector Parker/Xtras/f0.bin") })
        assertEquals(9, oneXtraLess.forGame("inspector_parker")!!.dirDiffs[0].expectedFiles)
        assertEquals(8, oneXtraLess.forGame("inspector_parker")!!.dirDiffs[0].actualFiles)
    }

    @Test
    fun absentGameIsMarkedNotFound() {
        val s = PackScanner.scan(perfect().filter { !it.path.startsWith("Bejeweled 2/") })
        assertEquals(GameStatus.TIDAK_ADA, status(s, "bejeweled2"))
        assertEquals(14, s.games.count { it.status == GameStatus.LENGKAP })
    }
}
