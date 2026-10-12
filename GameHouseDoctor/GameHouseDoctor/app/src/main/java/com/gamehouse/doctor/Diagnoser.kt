package com.gamehouse.doctor

/** Menyusun laporan teks yang detail: ringkasan pemindaian, laporan per game/gejala, dan hasil analisis log. */
object Diagnoser {

    private const val BAR = "========================================"
    private const val LINE = "----------------------------------------"

    private fun rank(l: Level) = when (l) {
        Level.TINGGI -> 0
        Level.SEDANG -> 1
        Level.RENDAH -> 2
    }

    // ---------------------------------------------------------------- ringkasan pemindaian ZIP

    fun packSummary(scan: PackScan): String = buildString {
        val ok = scan.games.count { it.status == GameStatus.LENGKAP }
        val warn = scan.games.count { it.status == GameStatus.PERLU_PERHATIAN }
        val none = scan.games.count { it.status == GameStatus.TIDAK_ADA }
        appendLine("RINGKASAN PEMINDAIAN ZIP")
        appendLine(BAR)
        appendLine("Entri di arsip: ${scan.totalEntries}")
        if (scan.unsafeEntries > 0) appendLine("Entri tidak aman (dilewati): ${scan.unsafeEntries}")
        appendLine("Game dikenali: ${ok + warn} dari ${scan.games.size}")
        appendLine("Lengkap: $ok | Perlu perhatian: $warn | Tidak ditemukan: $none")
        appendLine()
        scan.games.forEach { g ->
            val note = when (g.status) {
                GameStatus.LENGKAP -> "semua file inti cocok dengan referensi"
                GameStatus.PERLU_PERHATIAN -> problemSummary(g)
                GameStatus.TIDAK_ADA -> "folder/EXE tidak ada"
            }
            appendLine("- ${g.manifest.folder}: ${g.status.label} ($note)")
        }
        if (scan.unknownExeFolders.isNotEmpty()) {
            appendLine()
            appendLine("EXE di folder yang tidak dikenal katalog: ${scan.unknownExeFolders.joinToString(", ")}")
        }
        appendLine()
        append(
            "Cara kerja: tiap file inti dibandingkan dengan ukuran dan CRC-32 dari ZIP referensi, dan jumlah file tiap subfolder dihitung. " +
                "File yang memang berubah saat bermain (Install.log, settings.ini/dat, log game, file simpanan) hanya dicek keberadaannya. " +
                "Perbedaan belum tentu berarti rusak (bisa versi lain atau sengaja diubah), tetapi layak dicurigai bila game bermasalah."
        )
    }

    private fun problemSummary(g: GameScan): String {
        val parts = ArrayList<String>()
        if (g.missing.isNotEmpty()) parts.add("${g.missing.size} file hilang")
        if (g.sizeDiffs.isNotEmpty()) parts.add("${g.sizeDiffs.size} ukuran beda")
        if (g.crcDiffs.isNotEmpty()) parts.add("${g.crcDiffs.size} isi beda")
        if (g.missingDirs.isNotEmpty()) parts.add("${g.missingDirs.size} folder hilang")
        if (g.dirDiffs.isNotEmpty()) parts.add("${g.dirDiffs.size} folder jumlah file beda")
        return parts.joinToString(", ")
    }

    private fun scanSection(sb: StringBuilder, scan: GameScan?) {
        sb.appendLine("HASIL PEMERIKSAAN FILE")
        sb.appendLine(LINE)
        if (scan == null) {
            sb.appendLine("Belum ada ZIP yang dipindai. Ketuk 'Pindai ZIP koleksi' di layar utama agar bagian ini terisi dengan fakta nyata dari file-mu.")
            sb.appendLine()
            return
        }
        sb.appendLine("Status: ${scan.status.label}")
        if (scan.status == GameStatus.TIDAK_ADA) {
            sb.appendLine("Folder '${scan.manifest.folder}' dengan ${scan.manifest.exe} tidak ditemukan di ZIP yang dipindai.")
            sb.appendLine()
            return
        }
        sb.appendLine("File ditemukan untuk game ini: ${scan.filesFound} dari ${scan.manifest.totalFiles} di referensi.")
        if (scan.missing.isNotEmpty()) sb.appendLine("HILANG: ${scan.missing.joinToString(", ")}")
        scan.sizeDiffs.forEach {
            sb.appendLine("UKURAN BEDA: ${it.name} (referensi ${formatSize(it.expected)} / ${it.expected} B, di ZIP ${formatSize(it.actual)} / ${it.actual} B)")
        }
        if (scan.crcDiffs.isNotEmpty()) sb.appendLine("ISI BEDA (ukuran sama, CRC-32 berbeda): ${scan.crcDiffs.joinToString(", ")}")
        if (scan.missingDirs.isNotEmpty()) sb.appendLine("FOLDER HILANG: ${scan.missingDirs.joinToString(", ")}")
        scan.dirDiffs.forEach { sb.appendLine("JUMLAH FILE BEDA di folder ${it.name}: referensi ${it.expectedFiles}, di ZIP ${it.actualFiles}") }
        if (scan.extraRootFiles.isNotEmpty()) sb.appendLine("File tambahan di folder game (tidak ada di referensi): ${scan.extraRootFiles.joinToString(", ")}")
        if (!scan.hasProblems) sb.appendLine("Semua file inti cocok dengan referensi (ukuran dan CRC-32).")
        sb.appendLine()
    }

    // ---------------------------------------------------------------- blok satu masalah

    private fun appendIssue(sb: StringBuilder, n: Int, issue: Issue, level: Level?, why: String?, game: GameProfile?, confirmed: Boolean) {
        sb.appendLine(LINE)
        sb.appendLine("[$n] ${issue.title}" + if (confirmed) "   << TERKONFIRMASI DARI LOG >>" else "")
        if (level != null) sb.appendLine("Tingkat risiko untuk game ini: ${level.label}")
        if (why != null) sb.appendLine("Kenapa untuk game ini: $why")
        sb.appendLine("Keyakinan: ${issue.confidence}")
        sb.appendLine()
        sb.appendLine("PENYEBAB")
        sb.appendLine(issue.cause)
        sb.appendLine()
        sb.appendLine("BUKTI")
        sb.appendLine(issue.evidence)
        sb.appendLine()
        sb.appendLine("GEJALA / POLA LOG")
        sb.appendLine(issue.signs)
        if (game != null && issue.id == "REG_KEYS" && game.regKeys.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("KUNCI REGISTRI UNTUK ${game.name.uppercase()} (dari Install.log):")
            game.regKeys.forEach { sb.appendLine("  $it") }
        }
        if (game != null && issue.id == "DATA_DIRS" && game.dataDirs.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("FOLDER DATA UNTUK ${game.name.uppercase()} (dari Install.log):")
            game.dataDirs.forEach { sb.appendLine("  $it") }
        }
        sb.appendLine()
        sb.appendLine("SOLUSI BERTAHAP")
        issue.steps.forEachIndexed { i, s -> sb.appendLine("  ${i + 1}. $s") }
        sb.appendLine()
        sb.appendLine("CARA MEMASTIKAN BERES: ${issue.verify}")
        sb.appendLine("JIKA MASIH GAGAL: ${issue.ifFails}")
        sb.appendLine()
    }

    // ---------------------------------------------------------------- laporan game

    fun gameReport(game: GameProfile, scan: GameScan?, symptom: Symptom?, confirmedIssues: Set<String> = emptySet()): String {
        val sb = StringBuilder()
        sb.appendLine("LAPORAN DOKTER: ${game.name.uppercase()}")
        sb.appendLine(BAR)
        if (symptom != null) sb.appendLine("Gejala yang dipilih: ${symptom.label}")
        sb.appendLine()

        sb.appendLine("PROFIL TEKNIS")
        sb.appendLine(LINE)
        sb.appendLine("Pengembang: ${game.vendor}")
        sb.appendLine("Tahun: ${game.year}")
        sb.appendLine("Teknologi: ${game.engine}")
        sb.appendLine("Persyaratan asli: ${game.requirements}")
        sb.appendLine("Ketergantungan: ${game.dependencies}")
        sb.appendLine()

        sb.appendLine("BASELINE (BUKTI DARI FILE DAN LAYAR YANG KAMU KIRIM)")
        sb.appendLine(LINE)
        sb.appendLine(game.baseline)
        sb.appendLine()

        scanSection(sb, scan)

        // susun risiko: perbedaan file menaikkan FILE_INTEGRITY; hasil log menaikkan isu terkonfirmasi
        val risks = game.risks.map { r ->
            if (r.issueId == "FILE_INTEGRITY" && scan != null && scan.hasProblems) {
                Risk(r.issueId, Level.TINGGI, "Pemindaian ZIP menemukan perbedaan pada game ini (lihat hasil pemeriksaan file). ${r.why}")
            } else r
        }
        val ordered = risks.withIndex().sortedWith(
            compareBy({ if (it.value.issueId in confirmedIssues) 0 else 1 }, { rank(it.value.level) }, { it.index })
        ).map { it.value }

        val matching: List<Risk>
        val others: List<Risk>
        if (symptom == null) {
            matching = ordered
            others = emptyList()
        } else {
            val m = ordered.filter { Issues.get(it.issueId)?.symptoms?.contains(symptom) == true }
            matching = m
            others = ordered.filter { it !in m }
        }

        sb.appendLine(if (symptom == null) "DAFTAR RISIKO DI WINLATOR 11.2 (urut dari paling mungkin)" else "PENYEBAB YANG COCOK DENGAN GEJALA (urut dari paling mungkin)")
        if (matching.isEmpty()) {
            sb.appendLine(LINE)
            sb.appendLine("Tidak ada risiko di basis pengetahuan yang cocok langsung dengan gejala ini untuk game ini. Tiga risiko teratas ditampilkan sebagai gantinya.")
        }
        val shown = if (matching.isEmpty()) ordered.take(3) else matching
        shown.forEachIndexed { i, r ->
            val issue = Issues.get(r.issueId) ?: return@forEachIndexed
            appendIssue(sb, i + 1, issue, r.level, r.why, game, r.issueId in confirmedIssues)
        }
        val rest = if (matching.isEmpty()) ordered.drop(3) else others
        if (rest.isNotEmpty()) {
            sb.appendLine(LINE)
            sb.appendLine("RISIKO LAIN UNTUK GAME INI (tidak ditampilkan penuh; pilih 'Laporan lengkap' untuk membacanya):")
            rest.forEach { r ->
                val t = Issues.get(r.issueId)?.title ?: r.issueId
                sb.appendLine("- [${r.level.label}] $t")
            }
            sb.appendLine()
        }

        sb.appendLine("PENGATURAN WINLATOR YANG DISARANKAN (urutan percobaan)")
        sb.appendLine(LINE)
        game.setup.forEachIndexed { i, s -> sb.appendLine("  ${i + 1}. $s") }
        sb.appendLine()

        sb.appendLine("URUTAN PEMECAHAN MASALAH")
        sb.appendLine(LINE)
        sb.appendLine("  1. Pindai ZIP dan pastikan status game ini 'Lengkap'. Perbaiki file dulu bila tidak.")
        sb.appendLine("  2. Aktifkan 'Enable Wine debug' dan 'Save logs to file' di Container Settings, jalankan game sampai gejala muncul, lalu pakai Analisis log.")
        val top = shown.take(3).mapNotNull { Issues.get(it.issueId)?.title }
        if (top.isNotEmpty()) sb.appendLine("  3. Tangani berurutan: ${top.joinToString("  ->  ")}")
        sb.appendLine("  4. Ubah SATU pengaturan per percobaan dan tutup game lewat Task Manager sebelum mencoba lagi.")
        sb.appendLine("  5. Matikan 'Enable Wine debug', 'Save logs to file', dan 'Box64 Logs' setelah selesai (membuat game lambat).")
        sb.appendLine("  6. Jika masih gagal, kirim hasil Analisis log dan laporan ini ke saya.")
        sb.appendLine()

        sb.appendLine("CATATAN KEJUJURAN")
        sb.appendLine(LINE)
        sb.append(
            "Yang TERVERIFIKASI: isi dan struktur file game, import table EXE, Install.log, serta fakta tentang Winlator 11.2 (registri container bawaan, rootfs, menu, daftar pengaturan) " +
                "yang dibaca langsung dari APK-nya. Yang DUGAAN TERARAH ditandai pada kolom Keyakinan tiap masalah. Belum ada satu pun solusi di sini yang diuji di perangkatmu; " +
                "aplikasi ini tidak bisa mengubah pengaturan Winlator atau menjalankan game, hanya menunjukkan penyebab dan langkahnya."
        )
        return sb.toString()
    }

    // ---------------------------------------------------------------- laporan log

    fun logReport(a: LogAnalysis): String {
        val sb = StringBuilder()
        sb.appendLine("HASIL ANALISIS LOG")
        sb.appendLine(BAR)
        sb.appendLine("Baris dibaca: ${a.linesScanned}")
        val names = a.gamesDetected.mapNotNull { Games.get(it)?.name }
        sb.appendLine("Game terdeteksi di log: " + if (names.isEmpty()) "(tidak ada nama EXE game di log)" else names.joinToString(", "))
        sb.appendLine("Jumlah temuan: ${a.findings.size}")
        sb.appendLine()
        if (a.findings.isEmpty()) {
            sb.appendLine(
                "Tidak ada pola error yang dikenali. Pastikan 'Enable Wine debug' dan 'Save logs to file' aktif SAAT game dijalankan sampai error muncul, " +
                    "dan pilih file log yang benar (bukan log Box64 saja). Kirim log itu ke saya bila tetap kosong."
            )
            return sb.toString()
        }
        if (a.findings.any { it.issueId == "FLASH_ACTIVEX" }) {
            sb.appendLine(">> HIPOTESIS FLASH TERKONFIRMASI: log memuat permintaan kelas Shockwave Flash yang tidak terdaftar. <<")
            sb.appendLine()
        }
        a.findings.forEachIndexed { i, f ->
            sb.appendLine("${i + 1}. ${f.title}")
            sb.appendLine("   Bukti: ${f.evidence}")
            sb.appendLine("   Ringkasan: ${f.advice}")
            if (f.issueId != null) {
                a.gamesDetected.mapNotNull { Games.get(it) }.forEach { g ->
                    g.risks.firstOrNull { it.issueId == f.issueId }?.let { r ->
                        sb.appendLine("   Untuk ${g.name}: ${r.why}")
                    }
                }
                sb.appendLine("   Detail penyebab dan solusi lengkap ada di bagian bawah.")
            }
            sb.appendLine()
        }
        val ids = a.findings.mapNotNull { it.issueId }.distinct()
        val game = a.gamesDetected.firstNotNullOfOrNull { Games.get(it) }
        ids.forEachIndexed { i, id ->
            val issue = Issues.get(id) ?: return@forEachIndexed
            appendIssue(sb, i + 1, issue, null, null, game, true)
        }
        return sb.toString()
    }

    /** Daftar gejala untuk menu pemilihan. */
    val symptomLabels: List<String> = Symptom.values().map { it.label }
}
