package com.gamehouse.doctor

enum class Level(val label: String) { TINGGI("TINGGI"), SEDANG("SEDANG"), RENDAH("RENDAH") }

enum class Symptom(val label: String) {
    OLE_ERROR("Muncul 'OLE error 80040154' / Class not registered"),
    CRASH_START("Game langsung tertutup atau crash saat dibuka"),
    BLACK_SCREEN("Layar hitam / tidak ada gambar"),
    WRONG_VIDEO("Gambar rusak, warna aneh, atau panel kosong"),
    NO_SOUND("Tidak ada suara/musik, atau suara putus-putus"),
    MOUSE("Kursor tidak sinkron, tidak bisa klik, atau terkunci"),
    SLOW("Lambat atau patah-patah"),
    MISSING_FILE("Pesan file atau DLL tidak ditemukan"),
    NO_SAVE("Skor/progres/pengaturan tidak tersimpan"),
    RESOLUTION("Gagal mengatur resolusi / ukuran layar salah"),
    HANG("Macet (freeze) saat mulai atau keluar"),
    LICENSE("Muncul layar trial / registrasi")
}

/** Satu jenis masalah beserta penyebab mekanis, bukti, gejala, solusi bertahap, dan cara verifikasi. */
data class Issue(
    val id: String,
    val title: String,
    val symptoms: Set<Symptom>,
    /** Seberapa yakin penyebab dan solusinya, ditulis jujur. */
    val confidence: String,
    val cause: String,
    val evidence: String,
    val signs: String,
    val steps: List<String>,
    val verify: String,
    val ifFails: String
)

/** Risiko suatu masalah untuk game tertentu, dengan alasan khusus game itu. */
data class Risk(val issueId: String, val level: Level, val why: String)

data class GameProfile(
    val id: String,
    val name: String,
    val vendor: String,
    val year: String,
    val engine: String,
    val requirements: String,
    val dependencies: String,
    val baseline: String,
    val regKeys: List<String>,
    val dataDirs: List<String>,
    val setup: List<String>,
    val risks: List<Risk>
)
