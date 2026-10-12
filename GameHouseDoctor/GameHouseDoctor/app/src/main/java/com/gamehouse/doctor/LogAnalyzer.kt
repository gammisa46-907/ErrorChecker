package com.gamehouse.doctor

data class Finding(val title: String, val evidence: String, val advice: String, val issueId: String?)

class LogAnalysis(val findings: List<Finding>, val linesScanned: Int, val gamesDetected: List<String>)

/**
 * Membaca log Wine/Winlator (hasil 'Save logs to file') dan menerjemahkan pola error menjadi temuan
 * yang terhubung ke basis pengetahuan (Issues). Murni Kotlin, bisa diuji di JVM.
 */
object LogAnalyzer {

    private const val G = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

    private val CLASS_LINE = Regex("not registered|no class object|no instance created|80040154", RegexOption.IGNORE_CASE)
    private val CLASS_GUID = Regex("class(?:\\s+object)?\\s+\\{($G)\\}", RegexOption.IGNORE_CASE)
    private val MISSING_DLL = Regex("Library\\s+([A-Za-z0-9_.\\-]+)\\s+\\(which is needed by", RegexOption.IGNORE_CASE)

    private class Known(val name: String, val issueId: String?, val note: String)

    private val FLASH = Known(
        "Shockwave Flash (ActiveX)", "FLASH_ACTIVEX",
        "Terkonfirmasi: game meminta kontrol Flash yang tidak terdaftar di container."
    )
    private val IE = Known(
        "Internet Explorer / HTML", "HTML_CONTROL",
        "Kelas browser seharusnya terdaftar di Winlator; periksa Wine Gecko dan keutuhan registri."
    )
    private val STOCK_REGISTERED = "Kelas ini TERDAFTAR di container bawaan Winlator 11.2, jadi kehilangannya berarti registri container diubah/rusak atau container berbeda. " +
        "Coba buat container baru dan jalankan game di sana, atau kembalikan komponen terkait ke Builtin di Win Components."

    private val EXACT: Map<String, Known> = mapOf(
        "d27cdb6e-ae6d-11cf-96b8-444553540000" to FLASH,
        "8856f961-340a-11d0-a96b-00c04fd705a2" to IE,
        "eab22ac3-30c1-11cf-a7eb-0000c05bae0b" to IE,
        "25336920-03f9-11cf-8fd0-00aa00686f13" to IE,
        "636b9f10-0c7d-11d1-95b2-0020afdc7421" to Known("DirectMusic", null, STOCK_REGISTERED),
        "62be5d10-60eb-11d0-bd3b-00a0c911ce86" to Known("DirectShow", null, STOCK_REGISTERED),
        "d1eb6d20-8923-11d0-9d97-00a0c90a43cb" to Known("DirectPlay", null, STOCK_REGISTERED),
        "6bf52a52-394a-11d3-b153-00c04f79faa6" to Known("Windows Media Player (WMPlayer.OCX)", null, STOCK_REGISTERED),
        "0e59f1d5-1fbe-11d0-8ff2-00a0d10038bc" to Known("Microsoft Script Control", null, STOCK_REGISTERED)
    )

    private fun lookup(guid: String): Known? {
        EXACT[guid]?.let { return it }
        if (guid.endsWith("-b39b-11d1-8704-00600893b1bd")) return Known("DirectMusic", null, STOCK_REGISTERED)
        if (guid.startsWith("e436ebb")) return Known("DirectShow", null, STOCK_REGISTERED)
        return null
    }

    /** DLL pendamping yang diketahui dari import table game di pack ini. */
    private val PACK_DLLS = setOf(
        "academyres.dll", "levels.bin", "astrobaticsres.dll", "beje2res.dll", "resball.dll", "igglepopres.dll", "sdl.dll", "sdl_i.dll",
        "parkerres.dll", "whatresource.dll", "tapajamres.dll", "vorbis.dll", "tennistitansres.dll", "tradewinds2.dll", "gd204.dll",
        "bass.dll", "domres.dll", "joltresources.dll", "wordjong.dll", "msvcp60.dll"
    )

    private val DLL_COMPONENT: List<Pair<Regex, String>> = listOf(
        Regex("^(d3dx9_\\d+|d3dx10(_\\d+)?|d3dx11_\\d+|d3dcompiler_\\d+|d3dcsx_\\d+)(\\.dll)?$") to "Direct3D",
        Regex("^(dmband|dmcompos|dmime|dmloader|dmscript|dmstyle|dmsynth|dmusic|dmusic32|dswave)(\\.dll)?$") to "DirectMusic",
        Regex("^(amstream|qasf|qcap|qdvd|qedit|quartz)(\\.dll)?$") to "DirectShow",
        Regex("^(dplayx|dpmodemx|dpnet|dpnhpast|dpnhupnp|dpwsockx)(\\.dll)?$") to "DirectPlay",
        Regex("^(xaudio2_\\d+|x3daudio1_\\d+|xactengine\\d_\\d+|xapofx1_\\d+)(\\.dll)?$") to "XAudio",
        Regex("^(atl80|msvcm80|msvcp80|msvcr80|vcomp)(\\.dll)?$") to "vcrun2005",
        Regex("^(msvcp100|msvcr100|vcomp100|atl100)(\\.dll)?$") to "vcrun2010",
        Regex("^(wmvcore|wmasf|wmadmod|wmvdecod)(\\.dll)?$") to "wmdecoder"
    )

    private class Pattern(val key: String, val regex: Regex, val title: String, val issueId: String, val advice: String)

    private val PATTERNS: List<Pattern> = listOf(
        Pattern("ddraw", Regex("err:ddraw", RegexOption.IGNORE_CASE), "Wine melaporkan error DirectDraw", "DDRAW_DISPLAY",
            "Masalah mode layar/permukaan DirectDraw lama."),
        Pattern("d3d", Regex("err:d3d", RegexOption.IGNORE_CASE), "Wine melaporkan error Direct3D", "D3D8_RENDER",
            "Masalah pembuatan device Direct3D."),
        Pattern("audio", Regex("err:(dsound|wave|winmm|alsa|pulse)", RegexOption.IGNORE_CASE), "Wine melaporkan error audio", "AUDIO_WAVEOUT",
            "Masalah perangkat/driver audio."),
        Pattern("gecko", Regex("gecko|err:mshtml", RegexOption.IGNORE_CASE), "Log menyebut Gecko atau mshtml", "HTML_CONTROL",
            "Kontrol HTML membutuhkan Wine Gecko."),
        Pattern("crash", Regex("unhandled exception|SIGSEGV|SIGILL|segmentation fault", RegexOption.IGNORE_CASE), "Proses crash (exception tak tertangani / sinyal Box64)", "PACKED_EXE",
            "Crash saat start sering terkait EXE terbungkus pada emulasi; bukan pasti penyebabnya.")
    )

    private class Acc(val title: String, val advice: String, val issueId: String?, val evidence: String, var count: Int = 1)

    private fun record(map: MutableMap<String, Acc>, key: String, title: String, advice: String, issueId: String?, line: String) {
        val acc = map[key]
        if (acc == null) map[key] = Acc(title, advice, issueId, line.trim().take(160)) else acc.count++
    }

    fun analyze(lines: Sequence<String>): LogAnalysis {
        var scanned = 0
        val found = LinkedHashMap<String, Acc>()
        val games = LinkedHashSet<String>()
        val exeToGame = GameCatalog.all.associate { it.exe.lowercase() to it.id }

        for (line in lines) {
            scanned++
            val lower = line.lowercase()
            if (lower.contains(".exe")) {
                for ((exe, id) in exeToGame) if (lower.contains(exe)) games.add(id)
            }
            if (CLASS_LINE.containsMatchIn(line)) {
                val guid = CLASS_GUID.find(line)?.groupValues?.get(1)?.lowercase()
                if (guid != null) {
                    val known = lookup(guid)
                    if (known != null) {
                        record(found, "clsid:${known.name}", "Kelas COM tidak terdaftar: ${known.name}", known.note, known.issueId, line)
                    } else {
                        record(
                            found, "clsid:$guid", "Kelas COM tidak dikenal: {$guid}",
                            "Cari 'CLSID {$guid}' di internet untuk tahu komponennya, atau kirim GUID ini ke saya.", null, line
                        )
                    }
                }
            }
            val dll = MISSING_DLL.find(line)?.groupValues?.get(1)?.lowercase()
            if (dll != null) {
                val comp = DLL_COMPONENT.firstOrNull { it.first.matches(dll) }?.second
                when {
                    dll in PACK_DLLS -> record(
                        found, "dll:$dll", "DLL pendamping game tidak ditemukan: $dll",
                        "File ini bagian dari game. Pastikan ada di folder yang sama dengan EXE dan cocok dengan referensi (Pindai ZIP).", "LOADER_DLL", line
                    )
                    comp != null -> record(
                        found, "dll:$dll", "DLL komponen Windows tidak ditemukan: $dll",
                        "Container Settings -> Win Components -> $comp. Aktifkan/ubah komponen itu, lalu jalankan ulang.", null, line
                    )
                    else -> record(
                        found, "dll:$dll", "DLL tidak ditemukan: $dll",
                        "DLL ini tidak ada di komponen Winlator. Pastikan file ikut tersalin di folder game, atau kirim namanya ke saya.", "LOADER_DLL", line
                    )
                }
            }
            for (p in PATTERNS) {
                if (p.regex.containsMatchIn(line)) record(found, "pat:${p.key}", p.title, p.advice, p.issueId, line)
            }
        }
        val findings = found.values.map {
            Finding(it.title, if (it.count > 1) "${it.evidence}  (muncul ${it.count} kali)" else it.evidence, it.advice, it.issueId)
        }
        return LogAnalysis(findings, scanned, games.toList())
    }
}
