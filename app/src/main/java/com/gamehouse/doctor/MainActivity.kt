package com.gamehouse.doctor

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.concurrent.Executors

/**
 * GameHouse Doctor: pemeriksa error untuk 15 game GameHouse. Tidak menjalankan game dan tidak mengubah
 * pengaturan Winlator; hanya memindai file, menganalisis log, dan menjelaskan penyebab serta solusi.
 */
class MainActivity : Activity() {

    private lateinit var statusView: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var buttons: List<Button>

    private val executor = Executors.newSingleThreadExecutor()
    private var busy = false
    private var lastScan: PackScan? = null
    private var lastLog: LogAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val header = TextView(this).apply {
            text = "Pemeriksa error untuk 15 game GameHouse. Mulai dari tombol 1 agar laporan memuat fakta dari file-mu."
            setPadding(0, 0, 0, 16)
        }
        val b1 = Button(this).apply { text = "1. Pindai ZIP koleksi"; setOnClickListener { pickFile(REQ_PICK_ZIP) } }
        val b2 = Button(this).apply { text = "2. Diagnosa game dan gejala"; setOnClickListener { chooseGame() } }
        val b3 = Button(this).apply { text = "3. Analisis log Winlator"; setOnClickListener { pickFile(REQ_PICK_LOG) } }
        val b4 = Button(this).apply { text = "4. Diagnostik perangkat"; setOnClickListener { showDevice() } }
        buttons = listOf(b1, b2, b3, b4)

        statusView = TextView(this).apply { setPadding(0, 16, 0, 16) }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
        listView = ListView(this).apply {
            this.adapter = this@MainActivity.adapter
            visibility = View.GONE
            setOnItemClickListener { _, _, position, _ ->
                val scan = lastScan ?: return@setOnItemClickListener
                if (position == 0) {
                    ReportActivity.open(this@MainActivity, "Ringkasan pemindaian", Diagnoser.packSummary(scan))
                } else {
                    val g = scan.games.getOrNull(position - 1) ?: return@setOnItemClickListener
                    openGameReport(g.manifest.id, null)
                }
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            addView(header)
            buttons.forEach { addView(it) }
            addView(statusView)
            addView(listView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        setContentView(root)
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    private fun ui(block: () -> Unit) {
        runOnUiThread { if (!isFinishing && !isDestroyed) block() }
    }

    private fun setBusy(value: Boolean, message: String? = null) {
        busy = value
        buttons.forEach { it.isEnabled = !value }
        if (message != null) statusView.text = message
    }

    private fun errorText(e: Exception): String = when (e) {
        is SecurityException -> "Izin akses ke file ditolak"
        is FileNotFoundException -> "File tidak ditemukan atau tidak bisa dibuka (${e.message})"
        else -> "${e.javaClass.simpleName}: ${e.message}"
    }

    private fun openStream(uri: Uri): InputStream =
        contentResolver.openInputStream(uri) ?: throw FileNotFoundException("provider file tidak bisa membuka file ini")

    // ---------------------------------------------------------------- memilih file

    @Suppress("DEPRECATION")
    private fun pickFile(code: Int) {
        if (busy) return
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        try {
            startActivityForResult(intent, code)
        } catch (e: Exception) {
            statusView.text = "Tidak bisa membuka pemilih file: ${e.message}"
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Dipakai agar tanpa dependensi AndroidX")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_PICK_ZIP && requestCode != REQ_PICK_LOG) return
        if (resultCode != RESULT_OK) {
            statusView.text = "Pemilihan dibatalkan."
            return
        }
        val uri = data?.data
        if (uri == null) {
            statusView.text = "Pemilih file tidak mengembalikan file."
            return
        }
        if (requestCode == REQ_PICK_ZIP) scanZip(uri) else analyzeLog(uri)
    }

    // ---------------------------------------------------------------- 1. pindai ZIP

    private fun scanZip(uri: Uri) {
        setBusy(true, "Memindai ZIP (membaca seluruh isi, bisa beberapa detik sampai puluhan detik)...")
        executor.execute {
            try {
                when (val r = openStream(uri).use { ZipLister.list(it) }) {
                    is ZipResult.Failure -> ui { setBusy(false, "GAGAL membaca ZIP:\n${r.message}") }
                    is ZipResult.Success -> {
                        val scan = PackScanner.scan(r.items)
                        ui {
                            lastScan = scan
                            showScan(scan)
                        }
                    }
                }
            } catch (e: Exception) {
                ui { setBusy(false, "GAGAL memindai: ${errorText(e)}") }
            }
        }
    }

    private fun showScan(scan: PackScan) {
        val ok = scan.games.count { it.status == GameStatus.LENGKAP }
        val warn = scan.games.count { it.status == GameStatus.PERLU_PERHATIAN }
        val none = scan.games.count { it.status == GameStatus.TIDAK_ADA }
        adapter.clear()
        adapter.add("Ringkasan pemindaian ZIP (ketuk untuk membaca)")
        scan.games.forEach { g ->
            adapter.add("${g.manifest.folder}\n${g.status.label}")
        }
        adapter.notifyDataSetChanged()
        listView.visibility = View.VISIBLE
        setBusy(false, "Pemindaian selesai: lengkap $ok, perlu perhatian $warn, tidak ditemukan $none. Ketuk game untuk laporan lengkap.")
    }

    // ---------------------------------------------------------------- 2. diagnosa game dan gejala

    private fun chooseGame() {
        if (busy) return
        val names = Games.all.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Pilih game")
            .setItems(names) { _, which -> chooseSymptom(Games.all[which].id) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun chooseSymptom(gameId: String) {
        val labels = (listOf("Laporan lengkap (semua risiko)") + Diagnoser.symptomLabels).toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Gejala yang kamu alami")
            .setItems(labels) { _, which ->
                openGameReport(gameId, if (which == 0) null else Symptom.values()[which - 1])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openGameReport(gameId: String, symptom: Symptom?) {
        val game = Games.get(gameId) ?: return
        val scan = lastScan?.forGame(gameId)
        val log = lastLog
        val confirmed = if (log != null && (log.gamesDetected.isEmpty() || gameId in log.gamesDetected)) {
            log.findings.mapNotNull { it.issueId }.toSet()
        } else emptySet()
        val body = Diagnoser.gameReport(game, scan, symptom, confirmed)
        ReportActivity.open(this, "Laporan: ${game.name}", body)
    }

    // ---------------------------------------------------------------- 3. analisis log

    private fun analyzeLog(uri: Uri) {
        setBusy(true, "Menganalisis log...")
        executor.execute {
            try {
                val analysis = openStream(uri).bufferedReader(Charsets.ISO_8859_1).useLines { LogAnalyzer.analyze(it) }
                val body = Diagnoser.logReport(analysis)
                ui {
                    lastLog = analysis
                    setBusy(false, "Analisis log selesai: ${analysis.findings.size} temuan.")
                    ReportActivity.open(this, "Hasil analisis log", body)
                }
            } catch (e: Exception) {
                ui { setBusy(false, "GAGAL membaca log: ${errorText(e)}") }
            }
        }
    }

    // ---------------------------------------------------------------- 4. diagnostik perangkat

    private fun showDevice() {
        if (busy) return
        ReportActivity.open(this, "Diagnostik perangkat", DeviceProbe.report(this))
    }

    companion object {
        private const val REQ_PICK_ZIP = 1001
        private const val REQ_PICK_LOG = 1003
    }
}
