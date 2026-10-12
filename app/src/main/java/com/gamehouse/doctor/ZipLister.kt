package com.gamehouse.doctor

import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/** Satu entri di dalam arsip ZIP. [crc] adalah CRC-32 isi file (0 untuk folder). */
data class ZipItem(
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val crc: Long,
    /** true jika path berpotensi keluar dari folder tujuan saat diekstrak (zip-slip). */
    val unsafe: Boolean
)

sealed class ZipResult {
    data class Success(val items: List<ZipItem>) : ZipResult()
    data class Failure(val message: String) : ZipResult()
}

/**
 * Membaca daftar isi ZIP dari InputStream secara streaming (tanpa menyalin ke penyimpanan).
 * Seluruh isi dibaca sekali agar ukuran asli dan CRC-32 tiap file diketahui (arsip dengan
 * "data descriptor" tidak mencantumkan keduanya di header). ZipInputStream otomatis melempar
 * ZipException jika CRC data tidak cocok, sehingga arsip yang rusak terdeteksi.
 */
object ZipLister {

    fun list(input: InputStream): ZipResult {
        val items = ArrayList<ZipItem>()
        try {
            ZipInputStream(input).use { zis ->
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val entry = zis.nextEntry ?: break
                    var total = 0L
                    if (!entry.isDirectory) {
                        while (true) {
                            val n = zis.read(buf)
                            if (n < 0) break
                            total += n
                        }
                    }
                    zis.closeEntry()
                    val size = if (entry.isDirectory) 0L else if (entry.size >= 0) entry.size else total
                    items.add(
                        ZipItem(
                            path = entry.name,
                            isDirectory = entry.isDirectory,
                            size = size,
                            crc = if (entry.isDirectory || entry.crc < 0) 0L else entry.crc,
                            unsafe = isUnsafePath(entry.name)
                        )
                    )
                }
            }
        } catch (e: ZipException) {
            return ZipResult.Failure("Arsip ZIP rusak atau tidak didukung: ${e.message}")
        } catch (e: IOException) {
            return ZipResult.Failure("Gagal membaca arsip: ${e.message}")
        } catch (e: IllegalArgumentException) {
            return ZipResult.Failure("Nama file dalam arsip tidak bisa dibaca (encoding tidak didukung).")
        }
        if (items.isEmpty()) {
            return ZipResult.Failure("File ini bukan arsip ZIP yang valid, atau arsipnya kosong.")
        }
        return ZipResult.Success(items)
    }

    fun isUnsafePath(name: String): Boolean {
        val n = name.replace('\\', '/')
        if (n.startsWith("/")) return true
        if (Regex("^[A-Za-z]:").containsMatchIn(n)) return true
        return n.split('/').any { it == ".." }
    }
}
