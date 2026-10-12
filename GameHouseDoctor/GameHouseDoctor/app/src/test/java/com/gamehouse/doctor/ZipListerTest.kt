package com.gamehouse.doctor

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZipListerTest {

    /** Entri tanpa ukuran/CRC di header (memakai data descriptor), sama seperti ZIP game referensi. */
    private fun makeZip(vararg entries: Pair<String, ByteArray?>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { z ->
            for ((name, data) in entries) {
                z.putNextEntry(ZipEntry(name))
                if (data != null) z.write(data)
                z.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    private fun success(bytes: ByteArray): List<ZipItem> {
        val r = ZipLister.list(ByteArrayInputStream(bytes))
        assertTrue("Diharapkan Success tetapi: $r", r is ZipResult.Success)
        return (r as ZipResult.Success).items
    }

    @Test
    fun listsFilesAndFoldersWithRealSizes() {
        val items = success(
            makeZip(
                "Game/" to null,
                "Game/Game.exe" to ByteArray(1000) { (it % 251).toByte() },
                "Game/data/a.dat" to ByteArray(2500) { 7 }
            )
        )
        assertEquals(3, items.size)
        assertEquals(1, items.count { it.isDirectory })
        assertEquals(1000L, items.first { it.path == "Game/Game.exe" }.size)
        assertEquals(2500L, items.first { it.path == "Game/data/a.dat" }.size)
        assertFalse(items.any { it.unsafe })
    }

    @Test
    fun capturesCrc32OfFileContent() {
        val data = ByteArray(777) { (it * 3).toByte() }
        val expected = CRC32().apply { update(data) }.value
        val items = success(makeZip("A/file.bin" to data))
        assertEquals(expected, items[0].crc)
    }

    @Test
    fun flagsUnsafePaths() {
        val items = success(makeZip("../evil.txt" to ByteArray(1), "/abs.txt" to ByteArray(1), "ok/file.txt" to ByteArray(1)))
        assertTrue(items.first { it.path == "../evil.txt" }.unsafe)
        assertTrue(items.first { it.path == "/abs.txt" }.unsafe)
        assertFalse(items.first { it.path == "ok/file.txt" }.unsafe)
    }

    @Test
    fun notAZipIsFailure() {
        assertTrue(ZipLister.list(ByteArrayInputStream("ini bukan zip".toByteArray())) is ZipResult.Failure)
    }

    @Test
    fun emptyInputIsFailure() {
        assertTrue(ZipLister.list(ByteArrayInputStream(ByteArray(0))) is ZipResult.Failure)
    }

    @Test
    fun corruptedDataIsFailure() {
        val data = ByteArray(200) { it.toByte() }
        val crc = CRC32().apply { update(data) }
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { z ->
            val e = ZipEntry("x.bin").apply {
                method = ZipEntry.STORED
                size = 200
                compressedSize = 200
                this.crc = crc.value
            }
            z.putNextEntry(e)
            z.write(data)
            z.closeEntry()
        }
        val bytes = bos.toByteArray()
        bytes[35 + 10] = (bytes[35 + 10].toInt() xor 0xFF).toByte()
        val r = ZipLister.list(ByteArrayInputStream(bytes))
        assertTrue("Diharapkan Failure tetapi: $r", r is ZipResult.Failure)
    }
}
