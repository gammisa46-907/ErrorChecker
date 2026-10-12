# GameHouse Doctor

Aplikasi Android khusus untuk **memeriksa error game GameHouse Pack** (15 game dari
`_GameHouse_Games_Collection.zip`) dan menjelaskan **penyebab** serta **solusi bertahap** yang detail.
Aplikasi TIDAK menjalankan game dan TIDAK mengubah pengaturan Winlator; ia memindai file, membaca log,
dan menjelaskan.

Game yang didukung: Academy of Magic, Adventure Inlay, Astrobatics, Bejeweled 2, Hamsterball, Iggle Pop!,
Incredible Ink, Inspector Parker, Super WHATword, Tap a Jam, Tennis Titans, Tradewinds 2, Ultimate Dominoes,
Word Jolt, WordJong To Go.

## Build lewat GitHub
Unggah SELURUH isi folder (termasuk `.github`), buka tab Actions, tunggu workflow **Build APK**, unduh APK dari
Artifacts. Aplikasi ini berpaket `com.gamehouse.doctor`, jadi bisa dipasang berdampingan dengan PC Game Manager lama.

## Cara memakai
1. **Pindai ZIP koleksi**: pilih ZIP game-mu. Aplikasi membaca seluruh isinya lalu membandingkan tiap file inti dengan
   ukuran dan CRC-32 dari ZIP referensi, dan menghitung jumlah file tiap subfolder. Hasil: Lengkap / Perlu perhatian / Tidak ditemukan.
2. **Diagnosa game dan gejala**: pilih game, lalu gejala (OLE error, layar hitam, tidak ada suara, dst.) atau 'Laporan lengkap'.
3. **Analisis log Winlator**: di Container Settings aktifkan 'Enable Wine debug' dan 'Save logs to file', jalankan game sampai
   error muncul, lalu pilih file lognya. Temuan dihubungkan ke penyebab dan solusi di basis pengetahuan.
4. **Diagnostik perangkat**: model, chipset, ABI, RAM, OpenGL ES, Vulkan, dan apakah Winlator terpasang.

## Dari mana datangnya pengetahuan
- Import table, seksi, dan isi 15 EXE; `readme.txt`, `Install.log`, dan log/pengaturan di dalam ZIP.
- APK Winlator 11.2: registri container bawaan (kelas COM mana yang terdaftar), isi rootfs, menu Start, label pengaturan,
  paket DX wrapper dan driver grafis, variabel Box64.
Setiap masalah diberi label keyakinan (TERVERIFIKASI atau DUGAAN TERARAH).

## Status (jujur)
| Bagian | Status |
|---|---|
| Algoritma pemindaian ZIP | Divalidasi dengan prototipe Python pada ZIP asli (9 skenario), kode Kotlin belum dikompilasi/diuji |
| Basis pengetahuan (18 masalah, 15 game) | Ditulis; fakta file/APK terverifikasi, perbaikan di Winlator BELUM diuji di perangkat |
| Unit test (7 file) | Ditulis, BELUM dijalankan |
| Aplikasi di HP | BELUM diuji |
