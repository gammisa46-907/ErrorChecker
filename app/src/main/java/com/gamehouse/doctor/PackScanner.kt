package com.gamehouse.doctor

enum class GameStatus(val label: String) {
    LENGKAP("Lengkap"),
    PERLU_PERHATIAN("Perlu perhatian"),
    TIDAK_ADA("Tidak ditemukan di ZIP")
}

data class SizeDiff(val name: String, val expected: Long, val actual: Long)
data class DirDiff(val name: String, val expectedFiles: Int, val actualFiles: Int)

class GameScan(
    val manifest: GameManifest,
    val status: GameStatus,
    val missing: List<String>,
    val sizeDiffs: List<SizeDiff>,
    /** Ukuran sama tetapi isi (CRC-32) berbeda dari referensi. */
    val crcDiffs: List<String>,
    val missingDirs: List<String>,
    val dirDiffs: List<DirDiff>,
    val extraRootFiles: List<String>,
    val filesFound: Int
) {
    val hasProblems: Boolean
        get() = status == GameStatus.PERLU_PERHATIAN
}

class PackScan(
    val games: List<GameScan>,
    val totalEntries: Int,
    val unsafeEntries: Int,
    /** Folder berisi .exe yang tidak dikenal oleh katalog. */
    val unknownExeFolders: List<String>
) {
    fun forGame(id: String): GameScan? = games.firstOrNull { it.manifest.id == id }
}

/** Membandingkan daftar isi ZIP dengan katalog referensi 15 game (ukuran, CRC-32, jumlah file per subfolder). */
object PackScanner {

    private class F(val path: String, val item: ZipItem) {
        val lower: String = path.lowercase()
    }

    fun scan(items: List<ZipItem>): PackScan {
        val files = items
            .filter { !it.isDirectory && !it.unsafe }
            .map { F(it.path.replace('\\', '/'), it) }
        val byLower = HashMap<String, F>()
        files.forEach { byLower[it.lower] = it }
        val matchedPrefixes = ArrayList<String>()

        val scans = GameCatalog.all.map { m ->
            val folderLower = m.folder.lowercase()
            val exeLower = m.exe.lowercase()
            val exeEntry = files.firstOrNull {
                it.lower == "$folderLower/$exeLower" || it.lower.endsWith("/$folderLower/$exeLower")
            }
            if (exeEntry == null) {
                GameScan(m, GameStatus.TIDAK_ADA, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0)
            } else {
                val prefix = exeEntry.path.substring(0, exeEntry.path.length - m.exe.length)
                val prefixLower = prefix.lowercase()
                matchedPrefixes.add(prefixLower)

                val missing = ArrayList<String>()
                val sizeDiffs = ArrayList<SizeDiff>()
                val crcDiffs = ArrayList<String>()
                for (spec in m.files) {
                    val e = byLower[prefixLower + spec.name.lowercase()]
                    if (e == null) {
                        missing.add(spec.name)
                    } else if (!spec.volatile) {
                        if (e.item.size != spec.size) {
                            sizeDiffs.add(SizeDiff(spec.name, spec.size, e.item.size))
                        } else if (e.item.crc != 0L && e.item.crc != spec.crc) {
                            crcDiffs.add(spec.name)
                        }
                    }
                }

                val missingDirs = ArrayList<String>()
                val dirDiffs = ArrayList<DirDiff>()
                for (d in m.dirs) {
                    val dp = prefixLower + d.name.lowercase() + "/"
                    val count = files.count { it.lower.startsWith(dp) }
                    if (count == 0 && d.files > 0) {
                        missingDirs.add(d.name)
                    } else if (!d.volatile && count != d.files) {
                        dirDiffs.add(DirDiff(d.name, d.files, count))
                    }
                }

                val known = m.files.map { it.name.lowercase() }.toSet()
                val extras = files
                    .filter { it.lower.startsWith(prefixLower) && !it.lower.substring(prefixLower.length).contains('/') }
                    .map { it.path.substring(prefix.length) }
                    .filter { it.lowercase() !in known }
                val found = files.count { it.lower.startsWith(prefixLower) }

                val ok = missing.isEmpty() && sizeDiffs.isEmpty() && crcDiffs.isEmpty() && missingDirs.isEmpty() && dirDiffs.isEmpty()
                GameScan(
                    m, if (ok) GameStatus.LENGKAP else GameStatus.PERLU_PERHATIAN,
                    missing, sizeDiffs, crcDiffs, missingDirs, dirDiffs, extras, found
                )
            }
        }

        val unknown = files
            .filter { it.lower.endsWith(".exe") }
            .filter { f -> matchedPrefixes.none { f.lower.startsWith(it) } }
            .map { f -> f.path.substringBeforeLast('/', "(akar arsip)") }
            .distinct()
        return PackScan(scans, items.size, items.count { it.unsafe }, unknown)
    }
}
