package com.gamehouse.doctor

/**
 * Basis pengetahuan masalah. Setiap fakta dilabeli jujur: yang DIVERIFIKASI dari file game, APK Winlator 11.2,
 * atau registri/rootfs bawaan Winlator, dibedakan dari DUGAAN TERARAH (pola umum yang belum diuji di perangkatmu).
 */
object Issues {

    private const val W = "Container Settings"

    val all: List<Issue> = listOf(

        Issue(
            id = "FLASH_ACTIVEX",
            title = "Kontrol ActiveX Shockwave Flash tidak terdaftar (OLE error 80040154)",
            symptoms = setOf(Symptom.OLE_ERROR, Symptom.CRASH_START),
            confidence = "Penyebab: DUGAAN KUAT (ada bukti di file game, registri, dan rootfs Winlator). Perbaikan: BELUM PERNAH DIUJI di perangkatmu.",
            cause = "Game dibuat dengan Delphi dan memuat kontrol ActiveX lewat COM. Saat game meminta kelas COM yang tidak ada di registri Wine, " +
                "sistem mengembalikan kode 0x80040154 (REGDB_E_CLASSNOTREG, 'Class not registered'). Delphi menampilkannya sebagai dialog " +
                "'OLE error 80040154.' lalu game berhenti. Kelas yang diminta adalah Shockwave Flash, CLSID {D27CDB6E-AE6D-11CF-96B8-444553540000}. " +
                "Winlator tidak menyertakan Adobe Flash Player, jadi kelas itu tidak pernah terdaftar.",
            evidence = "- EXE mengimpor ole32.dll dan oleaut32.dll serta fungsi OleRun (pola Delphi untuk menjalankan kontrol ActiveX).\n" +
                "- Di dalam data EXE ada potongan type library Flash: ekor GUID D27CDB6x-AE6D-11CF-96B8-444553540000 tepat di samping teks 'hockwaveFlash' " +
                "(huruf awalnya tertutup kompresi pembungkus). Ini bukan kebetulan: GUID itu khas objek Flash.\n" +
                "- Registri container bawaan Winlator 11.2 diperiksa: kelas IE WebBrowser, HTMLDocument, Windows Media Player, DirectMusic, DirectShow, " +
                "Script Control, DirectPlay, dan MSXML semuanya TERDAFTAR. Hanya Shockwave Flash yang TIDAK ada (CLSID maupun ProgID ShockwaveFlash.ShockwaveFlash).\n" +
                "- Rootfs Winlator 11.2 tidak memuat swflash.ocx.\n" +
                "- Belum terbukti: fungsi persis Flash di dalam game (menu, animasi, atau layar pembungkus).",
            signs = "Dialog berjudul nama game berisi 'OLE error 80040154.'; proses game tetap terlihat di Task Manager. Di log Wine (Enable Wine debug): " +
                "baris 'class {d27cdb6e-ae6d-11cf-96b8-444553540000} not registered' atau 'no instance created ... of class {d27cdb6e-...}'.",
            steps = listOf(
                "KONFIRMASI DULU. Di $W aktifkan 'Enable Wine debug' dan 'Save logs to file', jalankan game sampai error muncul, lalu pakai tombol Analisis log di aplikasi ini. " +
                    "Jika GUID yang tercatat BUKAN d27cdb6e-ae6d-11cf-96b8-444553540000, hentikan langkah Flash di bawah dan ikuti hasil analisis.",
                "Jika GUID cocok: game membutuhkan file swflash.ocx (Flash Player ActiveX 32-bit). Adobe menghentikan Flash pada akhir 2020 dan menarik unduhan resminya, " +
                    "jadi saya tidak bisa menunjuk sumber unduhan yang sah. Gunakan hanya salinan yang kamu miliki secara sah.",
                "Salin swflash.ocx ke folder C:\\windows\\syswow64 di container (game ini 32-bit, jadi pakai folder 32-bit). Pakai Start -> System Tools -> Computer " +
                    "(file manager Wine) atau salin lewat drive yang dipetakan di $W -> Drives.",
                "Daftarkan: Start -> System Tools -> Command Prompt, ketik  C:\\windows\\syswow64\\regsvr32.exe C:\\windows\\syswow64\\swflash.ocx  " +
                    "lalu Enter. Harus muncul pesan bahwa DllRegisterServer berhasil.",
                "Periksa: Start -> System Tools -> Registry Editor, buka HKEY_CLASSES_ROOT\\ShockwaveFlash.ShockwaveFlash. Kunci itu harus ada.",
                "Jalankan ulang game. Jika pesan error berubah, jalankan Analisis log lagi: itu penyebab berikutnya yang perlu ditangani."
            ),
            verify = "Dialog OLE error tidak muncul dan log tidak lagi menyebut GUID d27cdb6e-...",
            ifFails = "Setahu saya tidak ada pengganti untuk kontrol ActiveX Flash di Winlator. Jika Flash tidak bisa dipasang secara sah, game ini tidak akan berjalan " +
                "di Winlator; itu batas teknis, bukan kerusakan file. Game lain di pack ini tidak menunjukkan ketergantungan Flash pada tingkat file."
        ),

        Issue(
            id = "HTML_CONTROL",
            title = "Panel HTML/browser kosong atau macet (Wine Gecko belum terpasang)",
            symptoms = setOf(Symptom.WRONG_VIDEO, Symptom.HANG),
            confidence = "Penyebab: DUGAAN TERARAH. Fakta Winlator (tanpa Gecko, ada penginstal) TERVERIFIKASI dari APK dan rootfs.",
            cause = "Sebagian game GameHouse menampilkan halaman web (promo, beli, bantuan) lewat kontrol WebBrowser. Kelas COM-nya terdaftar di Winlator, tetapi mesin HTML " +
                "(mshtml) membutuhkan Wine Gecko untuk menggambar halaman. Tanpa Gecko, panel menjadi kosong, gagal dibuat, atau menunggu tanpa akhir.",
            evidence = "- Rootfs Winlator 11.2: ieframe.dll, mshtml.dll, shdocvw.dll ada, tetapi paket Gecko tidak ada.\n" +
                "- Menu Start Winlator punya penginstal 'Wine Gecko (For HTML Pages)'.\n" +
                "- Game yang mengimpor urlmon/wininet menunjukkan fitur jaringan; belum terbukti dipakai untuk tampilan HTML.",
            signs = "Kotak putih/kosong di dalam dialog, jeda lama saat membuka tautan, permintaan memasang Gecko, atau log yang menyebut 'gecko' atau 'mshtml'.",
            steps = listOf(
                "Start -> System Tools -> Installers -> 'Wine Gecko (For HTML Pages)' dan ikuti prosesnya (butuh internet; hasilnya hanya berlaku di container itu).",
                "Tutup game lewat Task Manager lalu buka lagi.",
                "Uji Gecko: Start -> Programs -> Internet Explorer dan buka halaman lokal sederhana. Jika kosong juga, pemasangan Gecko belum berhasil.",
                "Jika game hanya memakai panel itu untuk iklan/promo, kegagalan ini tidak menghalangi permainan dan bisa diabaikan."
            ),
            verify = "Internet Explorer Wine menampilkan halaman, dan panel di game tidak lagi kosong.",
            ifFails = "Kirim log (Enable Wine debug) dan cari baris yang menyebut mshtml atau gecko."
        ),

        Issue(
            id = "DDRAW_DISPLAY",
            title = "DirectDraw dan mode layar lama (fullscreen, 16-bit, ganti resolusi)",
            symptoms = setOf(Symptom.BLACK_SCREEN, Symptom.WRONG_VIDEO, Symptom.RESOLUTION, Symptom.CRASH_START),
            confidence = "Mekanisme: UMUM dan terdokumentasi. Daftar wrapper di Winlator 11.2: TERVERIFIKASI dari APK. Pengaruhnya pada game ini: BELUM DIUJI.",
            cause = "Game tahun 2001-2005 memakai DirectDraw untuk mengganti mode layar (640x480 atau 800x600) dengan warna 16-bit; semua readme di pack ini menyebut " +
                "'16-bit Graphics'. Layar virtual Winlator berwarna 32-bit dan tidak bisa berganti mode seperti kartu grafis lama. Permintaan mode 16-bit atau fullscreen eksklusif " +
                "dapat gagal sehingga muncul layar hitam, warna rusak, flicker, atau pesan seperti 'SetResolution Failed'.",
            evidence = "- Ultimate Dominoes meninggalkan errors.txt berisi 'SetResolution Failed 0'. File itu berasal dari uji di PC Windows (settings.ini Word Jolt di pack yang sama mencatat " +
                "kartu NVIDIA GTX 480), jadi kegagalan ini sudah muncul di luar Winlator dan besar kemungkinan muncul lagi.\n" +
                "- IgglePop.log mencatat 'ddraw.dll loaded ... Using ddraw blt' (rendering DirectDraw 2D, berjalan baik pada PC referensi).\n" +
                "- APK Winlator 11.2 memuat cnc-ddraw 6.6 dan D7VK 1.11, dan label pengaturan 'DDraw Wrapper' dengan pilihan 'CNC DDraw' dan 'D7VK'.",
            signs = "Layar hitam atau kotak kecil di sudut, warna berubah-ubah, dialog gagal mengatur resolusi, kursor meleset. Log: baris err:ddraw atau fixme:ddraw, SetDisplayMode, SetCooperativeLevel.",
            steps = listOf(
                "$W -> Screen Size: samakan dengan resolusi asli game (640x480 atau 800x600; lihat bagian Pengaturan Disarankan di laporan game). Layar virtual yang pas menghindari perubahan mode.",
                "$W -> DDraw Wrapper: pilih 'CNC DDraw' (cnc-ddraw 6.6). Wrapper ini meniru mode 16-bit dan mengurus fullscreen/penskalaan sendiri; ini perbaikan paling umum untuk game DirectDraw lama.",
                "Jika CNC DDraw memperburuk: ganti ke 'D7VK' (DirectDraw/Direct3D7 lewat Vulkan), lalu terakhir ke bawaan Wine.",
                "Aktifkan 'Force Fullscreen' bila gambar tampil kecil di sudut; matikan bila kursor meleset.",
                "Jika game punya opsi Windowed atau Layar Penuh di menunya, pilih Windowed untuk percobaan pertama.",
                "Setelah setiap perubahan, tutup game sepenuhnya lewat Task Manager sebelum mencoba lagi, agar konfigurasi baru terpakai. Ubah SATU hal per percobaan."
            ),
            verify = "Gambar tampil penuh dan warna normal, tidak ada pesan resolusi.",
            ifFails = "Aktifkan Enable Wine debug lalu pakai Analisis log; cari err:ddraw. Coba juga Graphics Driver lain (Turnip untuk GPU Adreno; Vortek, Zink, atau VirGL untuk GPU lain)."
        ),

        Issue(
            id = "D3D8_RENDER",
            title = "Direct3D 8: layar hitam, tekstur putih, atau crash saat membuat device",
            symptoms = setOf(Symptom.BLACK_SCREEN, Symptom.WRONG_VIDEO, Symptom.CRASH_START, Symptom.SLOW),
            confidence = "Fakta game dan paket Winlator: TERVERIFIKASI. Kombinasi pengaturan yang berhasil di HP-mu: BELUM DIKETAHUI (perlu dicoba).",
            cause = "Hamsterball dan Word Jolt memanggil Direct3DCreate8. Winlator menerjemahkan Direct3D lewat WineD3D (OpenGL/Vulkan) atau paket DXVK/D8VK. " +
                "Hasilnya bergantung pada kombinasi Graphics Driver dan DX Wrapper; kombinasi yang salah menghasilkan layar hitam, tekstur putih, atau crash.",
            evidence = "- Import table EXE: d3d8.dll dengan fungsi Direct3DCreate8 (Hamsterball, Word Jolt).\n" +
                "- APK Winlator 11.2 memuat dxvk-2.4.1 (berisi d3d8.dll) dan d8vk-1.0 (d3d8.dll), serta driver Turnip 26.2.0, Vortek 2.1, Zink 22.2.5, VirGL 23.1.9, Gladio 1.1.\n" +
                "- Menu Start Winlator punya alat 'Test Direct3D' dan 'GPUInfo'.",
            signs = "Layar hitam setelah logo, tekstur putih/kotak-kotak, crash tepat saat game membuat jendela 3D. Log: err:d3d, err:d3d8, atau pesan DXVK tentang device/adapter.",
            steps = listOf(
                "Start -> System Tools -> Test Direct3D. Jika kotak ujinya hitam, masalahnya ada di driver grafis, bukan di game.",
                "$W -> DX Wrapper: coba 'WineD3D' dulu (paling kompatibel untuk Direct3D 8 lama). Jika ada pilihan Renderer, coba OpenGL lalu Vulkan bergantian.",
                "Jika WineD3D lambat atau rusak: DX Wrapper -> 'DXVK' (paket 2.4.1 memuat d3d8).",
                "$W -> Graphics Driver: GPU Adreno -> Turnip. GPU lain -> Vortek, Zink, atau VirGL, satu per satu. Start -> System Tools -> GPUInfo menunjukkan GPU yang terdeteksi.",
                "Jika tekstur putih: naikkan 'Video Memory Size' satu tingkat dan coba 'Offscreen Rendering Mode' bergantian.",
                "Catat kombinasi yang berhasil. Ubah SATU hal per percobaan."
            ),
            verify = "Menu dan permainan tampil dengan tekstur benar dan stabil beberapa menit.",
            ifFails = "Aktifkan Enable Wine debug lalu Analisis log; kirim baris err:d3d dan nama GPU dari Diagnostik perangkat."
        ),

        Issue(
            id = "AUDIO_WAVEOUT",
            title = "Audio: senyap, putus-putus, atau terlambat (FMOD/waveOut/DirectSound)",
            symptoms = setOf(Symptom.NO_SOUND),
            confidence = "Import game: TERVERIFIKASI. Pilihan Audio Driver di Winlator: TERVERIFIKASI dari APK. Pengaruh pada game ini: BELUM DIUJI.",
            cause = "Banyak game GameHouse memakai FMOD atau waveOut (winmm/msacm32), sebagian DirectSound atau BASS. Wine meneruskan audio ke Android lewat ALSA atau PulseAudio. " +
                "Driver yang tidak cocok atau buffer kecil menghasilkan senyap, suara pecah, atau penundaan yang terasa pada game ritme.",
            evidence = "- Import waveOut* dan msacm32 pada Adventure Inlay, Incredible Ink, Super WHATword, Tap a Jam; readme menyebut FMOD.\n" +
                "- Import dsound.dll pada Hamsterball, Iggle Pop, Word Jolt, WordJong To Go; bass.dll pada Bejeweled 2, Hamsterball, Tradewinds 2.\n" +
                "- APK Winlator 11.2 punya pengaturan 'Audio Driver' (PulseAudio dan lainnya).",
            signs = "Tidak ada suara walau volume HP naik; suara berderak; musik terputus saat gambar berat; efek suara terlambat.",
            steps = listOf(
                "$W -> Audio Driver: ganti antara pilihan yang ada (ALSA dan PulseAudio), uji satu per satu.",
                "Win Components: pastikan DirectSound berada pada pilihan Builtin/Wine (bawaan).",
                "Cek pengaturan di dalam game: banyak game punya tombol Music/Sound di menu opsi (Word Jolt menyimpan music=1 dan sound=1 di settings.ini).",
                "Tutup aplikasi lain yang memakai audio dan matikan mode hemat daya HP.",
                "Untuk game ritme (Tap a Jam): kecilkan beban CPU (Screen Size lebih kecil) agar pengisian buffer audio tidak tersendat.",
                "Pastikan Vorbis.dll (Tap a Jam), bass.dll, dan SDL.dll ada di folder game (cek hasil Pindai ZIP)."
            ),
            verify = "Musik dan efek terdengar jernih dan sinkron dengan gambar.",
            ifFails = "Aktifkan Enable Wine debug dan cari baris err:wave, err:dsound, atau err:alsa/pulse; kirim ke saya."
        ),

        Issue(
            id = "MIDI_MUSIC",
            title = "Musik MIDI tidak terdengar",
            symptoms = setOf(Symptom.NO_SOUND),
            confidence = "Fakta Winlator: TERVERIFIKASI dari APK. Apakah game ini benar memakai MIDI: BELUM TERBUKTI.",
            cause = "Musik berformat MIDI membutuhkan synthesizer. Wine tidak punya synthesizer bawaan; Winlator 11.2 menyediakan pengaturan MIDI SoundFont dan menyertakan " +
                "berkas SONiVOX-EAS-GM-Wavetable.sf2.",
            evidence = "- Astrobatics/Exception.Log (dari uji referensi) mencatat 'Exception midi: false' di samping 'sound effect: true'. Artinya opsi MIDI tidak aktif/ tidak tersedia pada uji itu, " +
                "jadi absennya MIDI mungkin memang normal.\n" +
                "- APK Winlator 11.2: aset soundfont/SONiVOX-EAS-GM-Wavetable.sf2 dan pengaturan 'MIDI SoundFont' dengan tombol uji.",
            signs = "Efek suara ada tetapi musik latar tidak terdengar; tidak ada pesan error.",
            steps = listOf(
                "$W -> MIDI SoundFont: pilih soundfont bawaan (SONiVOX-EAS-GM-Wavetable) lalu tekan tombol uji soundfont untuk memastikan Winlator bisa memainkannya.",
                "Jalankan ulang game dan cek apakah musik muncul.",
                "Jika musik tetap tidak ada, kemungkinan game tidak memakai MIDI; lanjutkan ke penyebab Audio (AUDIO_WAVEOUT)."
            ),
            verify = "Tombol uji soundfont menghasilkan bunyi dan musik game terdengar.",
            ifFails = "Abaikan jika hanya musik latar yang hilang dan permainan normal."
        ),

        Issue(
            id = "SDL12_VIDEO",
            title = "SDL 1.2: driver video DirectX gagal membuat surface",
            symptoms = setOf(Symptom.BLACK_SCREEN, Symptom.CRASH_START, Symptom.WRONG_VIDEO),
            confidence = "Keberadaan SDL.dll: TERVERIFIKASI. Perbaikan variabel lingkungan: DUGAAN TERARAH (teknik umum untuk SDL 1.2 di Wine, belum diuji di sini).",
            cause = "SDL 1.2 di Windows memilih driver video 'directx' secara bawaan. Di Wine driver itu kadang gagal membuat surface DirectDraw sehingga layar hitam atau game tertutup, " +
                "sedangkan driver 'windib' (GDI) lebih stabil.",
            evidence = "- IncredibleInk.exe mengimpor SDL.dll dan sdl_i.dll; SDL.dll (224 KB) ikut di folder game. Ukuran itu khas SDL 1.2 (dugaan).\n" +
                "- Folder config berisi 13 file XML/gambar pengaturan yang dibaca game saat mulai.",
            signs = "Layar hitam atau jendela sesaat lalu hilang; log memuat err:ddraw saat SDL membuka video.",
            steps = listOf(
                "$W -> Environment Variables -> New Environment Variable: nama SDL_VIDEODRIVER, nilai windib. Jalankan ulang game.",
                "Jika masih hitam: ganti nilai menjadi directx, lalu coba tanpa variabel itu.",
                "Jika gambar muncul tetapi suara bermasalah: tambahkan SDL_AUDIODRIVER dengan nilai waveout atau dsound, satu per satu.",
                "Jika windib menghasilkan gambar kecil: pilih DDraw Wrapper 'CNC DDraw' dan sesuaikan Screen Size."
            ),
            verify = "Menu utama Incredible Ink tampil dan menanggapi mouse.",
            ifFails = "Aktifkan Enable Wine debug dan kirim baris err:ddraw atau pesan SDL."
        ),

        Issue(
            id = "DIRECTOR_RUNTIME",
            title = "Proyektor Macromedia Director: Xtras, cast eksternal, dan Shockwave 3D",
            symptoms = setOf(Symptom.CRASH_START, Symptom.BLACK_SCREEN, Symptom.RESOLUTION),
            confidence = "Isi folder: TERVERIFIKASI dari ZIP. Perilaku di Winlator: BELUM DIUJI (dugaan terarah dari cara kerja Director).",
            cause = "Inspector Parker dan Tennis Titans adalah proyektor Macromedia Director. Proyektor memuat Xtras (.x32) dan cast eksternal (.cct/.dcr) dari folder game; " +
                "satu Xtra yang hilang memunculkan dialog 'Xtra not found' atau mematikan fitur. Beberapa Xtra memakai API Win32 yang jarang diuji di Wine: budapi.x32 (Buddy API) " +
                "dan resolution.x32 (dari namanya, mengatur resolusi layar; ini dugaan). Tennis Titans memakai Shockwave 3D yang memilih sendiri renderer OpenGL/DirectX 7/software.",
            evidence = "- Inspector Parker: Parker.exe + Parker.dcr + 4 file x*.cct + folder Xtras berisi 9 Xtra: DirectSound.x32, Font Asset.x32, Font Xtra.x32, SWADCmpr.x32, Sound Control.x32, " +
                "Text Asset.x32, TextXtra.x32, budapi.x32, resolution.x32.\n" +
                "- Tennis Titans: folder components berisi code.cct, models.cct (4,6 MB), online.cct, sound.cct, textures.cct (7,5 MB), dan folder images berisi loader.swf serta moregreatgames.swf.\n" +
                "- Readme Tennis Titans: 'DirectX 7.0 or better, or OpenGL'. Install.log menyebut folder Macromedia\\Director MX 2004\\Tennis Titans di AppData.",
            signs = "Dialog 'Xtra not found', jendela hitam setelah splash, game berhenti di layar loading, atau layar berpindah ukuran lalu hilang.",
            steps = listOf(
                "Jalankan Pindai ZIP dan pastikan folder Xtras (9 file) dan components (5 file) dinyatakan lengkap.",
                "Jangan memindahkan atau mengganti nama folder Xtras/components; keduanya harus bersebelahan dengan EXE.",
                "$W -> Screen Size: 640x480 untuk Inspector Parker; 800x600 untuk Tennis Titans. Samakan dulu dengan resolusi asli sebelum menguji hal lain.",
                "DDraw Wrapper 'CNC DDraw' untuk Inspector Parker. Tennis Titans (3D): DX Wrapper 'WineD3D' dulu; bila layar hitam saat masuk pertandingan, coba 'DXVK'.",
                "Pastikan folder data yang dibuat installer ada dan bisa ditulis (lihat DATA_DIRS di laporan game).",
                "Jika dialog menyebut nama Xtra tertentu, catat namanya dan kirim ke saya."
            ),
            verify = "Game melewati splash dan menu utama tampil.",
            ifFails = "Aktifkan Enable Wine debug dan Box64 Logs; kirim log. Pesan 'Xtra' persis akan menentukan langkah berikutnya."
        ),

        Issue(
            id = "PACKED_EXE",
            title = "EXE terbungkus (wrapper/protector) tidak stabil di emulasi Box64",
            symptoms = setOf(Symptom.CRASH_START, Symptom.HANG, Symptom.BLACK_SCREEN),
            confidence = "Fakta bahwa EXE terbungkus: TERVERIFIKASI. Bahwa itu menjadi penyebab crash di perangkatmu: DUGAAN TERARAH. Nama variabel Box64: TERVERIFIKASI dari APK.",
            cause = "EXE game dibungkus: kode asli dibongkar di memori saat berjalan (kode yang mengubah dirinya sendiri) dan pembungkus memeriksa lingkungannya. Di emulasi x86 ke ARM (Box64 dynarec), " +
                "kode yang berubah sendiri dan pengaturan memori yang longgar dapat membuat crash saat mulai atau macet di layar awal.",
            evidence = "- Dari 15 EXE: 13 memiliki seksi bernama GARR/.garr (pembungkus GameHouse). Super WHATword dan Tap a Jam memakai pola lain: tujuh seksi dengan empat nama kosong dan seksi .adata.\n" +
                "- Academy of Magic: ukuran file 11,9 MB tetapi hanya 40 KB di dalam seksi kode (sisanya data terkompresi).\n" +
                "- env_vars.json Box64 di APK: BOX64_DYNAREC_SAFEFLAGS (bawaan 2), BIGBLOCK (1), STRONGMEM (0), CALLRET (1), X87DOUBLE (0), FASTNAN (1), FASTROUND (1), FORWARD (128), WAIT (1), NATIVEFLAGS (0), WEAKBARRIER (0).",
            signs = "Game tertutup tepat setelah dibuka tanpa pesan, macet di logo, atau error acak berbeda tiap percobaan. Box64 Logs memuat SIGSEGV atau SIGILL.",
            steps = listOf(
                "$W -> Box64 Preset: pilih preset yang menekankan kestabilan/kompatibilitas bila ada, lalu coba lagi.",
                "Jika perlu atur manual (Box64 Preset -> duplikat/edit): BOX64_DYNAREC_BIGBLOCK=0, lalu BOX64_DYNAREC_STRONGMEM=1 (naikkan ke 2 lalu 3 bila masih crash).",
                "Jika masih gagal: BOX64_DYNAREC_SAFEFLAGS=2 dan BOX64_DYNAREC_CALLRET=0.",
                "Jika game jalan tetapi hitungan/gambar aneh: BOX64_DYNAREC_X87DOUBLE=1.",
                "Aktifkan 'Box64 Logs' untuk satu percobaan dan lihat ada SIGSEGV/SIGILL atau tidak (matikan lagi sesudahnya karena memperlambat).",
                "Ubah SATU variabel per percobaan. Setiap langkah di atas menurunkan kecepatan, tetapi game casual 2D tahun 2004 ringan sehingga biasanya tetap lancar."
            ),
            verify = "Game melewati logo dan menu utama dan stabil beberapa menit tanpa tertutup sendiri.",
            ifFails = "Kirim log Box64 dan nama preset yang dipakai. Perhatikan bahwa pembungkus lisensi tidak akan saya bantu dilewati."
        ),

        Issue(
            id = "REG_KEYS",
            title = "Kunci registri installer tidak ada (status instalasi/lisensi/preferensi)",
            symptoms = setOf(Symptom.LICENSE, Symptom.CRASH_START, Symptom.NO_SAVE),
            confidence = "Daftar kunci: TERVERIFIKASI dari Install.log. Efeknya pada tiap game: DUGAAN TERARAH (nilai di dalam kunci tidak tercatat).",
            cause = "Installer GameHouse menulis kunci registri yang dibaca game (jalur instalasi, status trial/lisensi, preferensi). Install.log di setiap folder mencatat kunci itu. " +
                "Jika folder game disalin ke container baru tanpa menjalankan installer, kunci tidak ada: game bisa menganggap dirinya belum terpasang, mulai ulang dari mode awal, atau gagal membaca jalur.",
            evidence = "- Install.log tiap game mencantumkan pohon registri (Root 1 = HKEY_CURRENT_USER, Root 2 = HKEY_LOCAL_MACHINE). Daftar per game ada di laporan game.\n" +
                "- Hampir semua EXE mengimpor RegOpenKeyEx dan RegQueryValueEx.\n" +
                "- Install.log hanya mencatat NAMA kunci, bukan nilainya.",
            signs = "Game meminta registrasi lagi, kembali ke pengaturan awal, tidak menemukan data sendiri, atau berhenti sebelum jendela muncul.",
            steps = listOf(
                "Cara paling aman: jalankan installer asli game di container yang sama (kamu harus memilikinya secara sah) agar kunci dan nilainya dibuat dengan benar.",
                "Periksa kunci: Start -> System Tools -> Registry Editor. Game 32-bit membaca tampilan WOW6432Node, jadi cek HKEY_LOCAL_MACHINE\\Software\\Wow6432Node\\GameHouse selain HKEY_LOCAL_MACHINE\\Software\\GameHouse.",
                "Jika kunci tidak ada dan game gagal membaca jalur: buat kunci kosong dengan nama yang sama (daftar di laporan game). Nilai di dalamnya dibuat game sendiri saat pertama berjalan.",
                "Jika muncul layar trial atau registrasi: itu berasal dari pembungkus lisensi. Aplikasi ini tidak membantu melewatinya; gunakan kunci atau versi penuh yang sah."
            ),
            verify = "Game tidak lagi meminta registrasi/instalasi ulang dan mengingat pengaturan setelah ditutup.",
            ifFails = "Kirim tangkapan layar pesan yang muncul dan isi kunci GameHouse yang ada di Registry Editor."
        ),

        Issue(
            id = "DATA_DIRS",
            title = "Folder data pengguna tidak ada atau tidak bisa ditulis",
            symptoms = setOf(Symptom.NO_SAVE, Symptom.CRASH_START),
            confidence = "Daftar folder: TERVERIFIKASI dari Install.log. Efeknya: DUGAAN TERARAH.",
            cause = "Beberapa game menyimpan pengaturan dan progres di folder pengguna atau ProgramData yang dibuat installer. Folder yang tidak ada atau tidak bisa ditulis membuat game gagal menyimpan, " +
                "atau (pada proyektor Director) berhenti saat mulai.",
            evidence = "- Inspector Parker: C:\\ProgramData\\Oberon Media\\Inspector Parker dan C:\\users\\xuser\\AppData\\Roaming\\THIS NEEDS TO BE FIX. Nama terakhir adalah placeholder " +
                "dari installer yang gagal menyelesaikan sebuah variabel; ini anomali nyata di Install.log.\n" +
                "- Tennis Titans: C:\\users\\xuser\\AppData\\Roaming\\Macromedia\\Director MX 2004\\Tennis Titans.\n" +
                "- WordJong To Go: C:\\users\\xuser\\AppData\\Roaming\\GameBlend\\WordJong.\n" +
                "- Tradewinds 2 membaca settings.dat (44 byte) dan default_settings.dat (20 byte) di folder game; Tap a Jam menulis ke folder Saved Jams.",
            signs = "Progres hilang setelah ditutup, game tertutup sesaat setelah logo, atau pesan gagal membuat/menulis file.",
            steps = listOf(
                "Start -> System Tools -> Computer: telusuri C:\\users\\xuser\\AppData\\Roaming dan C:\\ProgramData, lalu buat folder yang tercantum di laporan game bila belum ada.",
                "Pastikan folder tidak read-only (klik kanan -> Properties) dan drive yang dipetakan di $W -> Drives tidak read-only.",
                "Inspector Parker: JANGAN mengubah nama folder 'THIS NEEDS TO BE FIX' sembarangan. Jika game gagal mulai dan C:\\ProgramData\\Oberon Media\\Inspector Parker tidak ada, buat folder itu dulu dan uji.",
                "Folder game sendiri harus bisa ditulis (settings.dat, Saved Jams, users). Jangan menaruh game di lokasi baca-saja."
            ),
            verify = "Pengaturan dan skor bertahan setelah game ditutup dan dibuka lagi.",
            ifFails = "Kirim hasil Analisis log dan daftar isi folder data yang kamu buat."
        ),

        Issue(
            id = "STALE_CONFIG",
            title = "File pengaturan membawa hasil deteksi GPU dari PC Windows lain",
            symptoms = setOf(Symptom.BLACK_SCREEN, Symptom.CRASH_START, Symptom.WRONG_VIDEO),
            confidence = "Isi settings.ini: TERVERIFIKASI. Bahwa game memakainya tanpa deteksi ulang: DUGAAN (belum diketahui).",
            cause = "Word Jolt/settings.ini berisi hasil deteksi Direct3D dari PC Windows: Driver=nvd3dum.dll, DeviceDesc=NVIDIA GeForce GTX 480, DriverVersion=8.14.13.9135, " +
                "MaxTextureWidth/Height=16383, format X8R8G8B8 dan A8R8G8B8, WindowedOK=1, FullscreenOK=1. Jika game memakai nilai tersimpan itu tanpa mendeteksi ulang di Winlator, " +
                "ia bisa meminta kapasitas atau format yang tidak ada di GPU virtual Winlator.",
            evidence = "- Isi settings.ini di ZIP (bagian [Direct3D]) dan WorkingDir bergaya Windows.\n" +
                "- File yang sama menyimpan music/sound/tips/skill dan daftar skor; menghapusnya menghilangkan skor.",
            signs = "Word Jolt layar hitam atau crash saat membuat Direct3D, padahal Test Direct3D di Winlator normal.",
            steps = listOf(
                "Salin settings.ini ke cadangan (di luar folder game).",
                "Ubah nama atau hapus settings.ini di folder game; game akan menulis ulang saat mulai (skor hilang).",
                "Jalankan game. Buka settings.ini yang baru: DeviceDesc harus menyebut GPU virtual Winlator, bukan GTX 480.",
                "Jika game normal, biarkan file baru. Jika tidak membantu, kembalikan cadangan dan lanjut ke penyebab D3D8_RENDER."
            ),
            verify = "settings.ini baru berisi nama GPU Winlator dan game tampil normal.",
            ifFails = "Kirim isi settings.ini yang baru bagian [Direct3D]."
        ),

        Issue(
            id = "ONLINE_CALLS",
            title = "Panggilan internet lama (cek pembaruan/iklan/laporan bug) menunggu timeout",
            symptoms = setOf(Symptom.HANG),
            confidence = "Import jaringan: TERVERIFIKASI. Bahwa itu penyebab macet di perangkatmu: DUGAAN TERARAH.",
            cause = "Game memanggil InternetOpenA/InternetOpenUrlA (wininet) atau urlmon untuk mengecek pembaruan, iklan, atau mengirim laporan bug. Server era 2000-an sebagian sudah mati; " +
                "tanpa jawaban, panggilan menunggu sampai timeout sehingga game tampak macet beberapa detik hingga menit.",
            evidence = "- Import wininet/urlmon: Academy of Magic (InternetOpenA, urlmon, wininet), Astrobatics (InternetOpenUrlA), Iggle Pop (InternetOpenUrlA), Tap a Jam (wininet).\n" +
                "- Astrobatics menyertakan SendBugReport.exe.",
            signs = "Layar membeku saat mulai atau keluar lalu pulih sendiri setelah 30 detik atau lebih.",
            steps = listOf(
                "Tunggu hingga 60 detik sebelum menyimpulkan game rusak; jika pulih sendiri, itu timeout jaringan.",
                "Bandingkan dengan jaringan aktif dan mode pesawat untuk melihat apakah waktu macetnya berubah.",
                "Eksperimen: $W -> Environment Variables -> WINEDLLOVERRIDES dengan nilai wininet=d (menonaktifkan wininet; game biasanya menganggap internet tidak ada). Uji dampaknya pada game.",
                "Jangan menjalankan SendBugReport.exe; itu hanya pengirim laporan."
            ),
            verify = "Game masuk menu dan keluar tanpa jeda panjang.",
            ifFails = "Kembalikan WINEDLLOVERRIDES ke kosong bila game jadi tidak stabil, dan kirim log."
        ),

        Issue(
            id = "MOUSE_INPUT",
            title = "Mouse/kursor: tidak sinkron, terkunci, atau tidak bisa klik",
            symptoms = setOf(Symptom.MOUSE),
            confidence = "Nama opsi: TERVERIFIKASI dari APK. Kombinasi yang cocok per game: BELUM DIUJI.",
            cause = "Game 3D seperti Hamsterball mengambil kendali mouse penuh (readme: kontrol mouse untuk operasi Windows biasa hilang saat bermain). Sentuhan layar di Winlator harus dipetakan ke kursor " +
                "absolut atau relatif; pilihan yang salah membuat kursor melompat, terjebak di sudut, atau klik meleset.",
            evidence = "- Readme Hamsterball menyebut hilangnya kendali mouse normal saat bermain; EXE memakai dinput8.dll (DirectInput8Create).\n" +
                "- APK Winlator 11.2 punya: Relative Mouse Movement, Capture pointer on external mouse, Mouse Warp Override, Move Cursor to Touchpoint, Disable Mouse Input, Cursor Size, Cursor Speed, Show Touchscreen Controls.",
            signs = "Kursor melompat ke satu sisi, bola/karakter bergerak sendiri, klik tidak mengenai tombol.",
            steps = listOf(
                "$W -> aktifkan 'Relative Mouse Movement' untuk game yang mengunci kursor (Hamsterball); matikan untuk game menu klik biasa.",
                "Coba 'Mouse Warp Override' bergantian (Enable/Disable/Force) bila kursor melompat.",
                "Matikan 'Move Cursor to Touchpoint' bila kursor pindah ke titik sentuh dan menimbulkan klik tak sengaja.",
                "Pastikan 'Disable Mouse Input' TIDAK aktif; atur 'Cursor Speed' lebih rendah bila terlalu sensitif.",
                "Gunakan 'Show Touchscreen Controls' dan mode trackpad bila perlu."
            ),
            verify = "Kursor mengikuti jari dan klik mengenai target.",
            ifFails = "Sambungkan mouse eksternal untuk memastikan masalahnya di pemetaan sentuhan, bukan di game."
        ),

        Issue(
            id = "FOCUS_LOSS",
            title = "Kehilangan fokus: game menjeda atau mengecil sendiri",
            symptoms = setOf(Symptom.MOUSE, Symptom.HANG),
            confidence = "Pola di log: TERVERIFIKASI (Astrobatics/Exception.Log). Penyebab Android: DUGAAN TERARAH.",
            cause = "Game Delphi menjeda saat jendelanya kehilangan fokus. Di Android, gestur navigasi, panel notifikasi, atau overlay dapat membuat jendela menerima onDeactivate lalu onActivate " +
                "berulang kali, sehingga game menjeda atau mengecil.",
            evidence = "- Astrobatics/Exception.Log mencatat 'onactivate' dan 'ondeactivate' bergantian beberapa kali dalam sekitar 40 detik (14:55:22, 14:55:58, 14:55:58, ...).",
            signs = "Game berhenti sebentar saat menyentuh tepi layar atau menarik notifikasi; musik terputus; jendela kembali setelah disentuh.",
            steps = listOf(
                "Matikan gestur navigasi dan notifikasi pop-up saat bermain (Jangan Ganggu).",
                "Hindari menyentuh tepi layar; mainkan dengan layar penuh.",
                "$W -> aktifkan 'Force Fullscreen' agar jendela game tidak berbagi fokus dengan desktop Wine.",
                "Jika game mengecil, buka Task Manager Wine dan fokuskan lagi jendelanya."
            ),
            verify = "Log game tidak lagi mencatat onactivate/ondeactivate berulang saat bermain.",
            ifFails = "Kirim isi Exception.Log setelah sesi bermain."
        ),

        Issue(
            id = "FILE_INTEGRITY",
            title = "File game hilang, ukurannya berbeda, atau isinya berbeda dari referensi",
            symptoms = setOf(Symptom.MISSING_FILE, Symptom.CRASH_START, Symptom.NO_SOUND),
            confidence = "Pemindaian ZIP: TERVERIFIKASI (ukuran dan CRC-32 dibandingkan dengan ZIP referensi). Arti perbedaan: perlu ditafsirkan.",
            cause = "Game memuat file inti dari folder yang sama dengan EXE. Satu file hilang atau terpotong (ZIP rusak, ekstraksi gagal, antivirus mengarantina) menyebabkan game gagal di titik tertentu. " +
                "File yang isinya berbeda dari referensi belum tentu rusak (bisa versi lain atau dimodifikasi), tetapi layak dicurigai bila game bermasalah.",
            evidence = "- Aplikasi membandingkan setiap file inti dengan ukuran dan CRC-32 dari ZIP referensi, dan jumlah file tiap subfolder. Hasil per game ada di bagian 'Hasil pemeriksaan file'.",
            signs = "Pesan file tidak ditemukan, game berhenti di loading, gambar/suara tertentu hilang.",
            steps = listOf(
                "Baca bagian 'Hasil pemeriksaan file' untuk daftar file yang hilang atau berbeda.",
                "Ekstrak ulang dari ZIP asli (jangan dari salinan yang sudah dimainkan) dengan aplikasi yang mendukung nama file berspasi dan huruf besar-kecil.",
                "Jika ZIP dibuat ulang di PC Windows, periksa karantina antivirus: game lama dengan pembungkus sering ditandai palsu dan EXE/DLL-nya dihapus.",
                "File yang sengaja berubah saat bermain (Install.log, settings.ini/dat, log game, file simpanan) dikecualikan dari perbandingan isi dan hanya dicek keberadaannya."
            ),
            verify = "Pindai ZIP ulang menyatakan game 'Lengkap'.",
            ifFails = "Kirim hasil pemindaian game itu ke saya."
        ),

        Issue(
            id = "LOADER_DLL",
            title = "DLL pendamping game tidak ditemukan saat EXE dimuat",
            symptoms = setOf(Symptom.MISSING_FILE, Symptom.CRASH_START),
            confidence = "Daftar DLL per game: TERVERIFIKASI dari import table EXE. Pesan Wine persis: lihat log.",
            cause = "Hampir setiap EXE di pack ini mengimpor DLL pendamping miliknya sendiri secara statis (misalnya academyres.dll, beje2res.dll, resball.dll). Jika file itu tidak ada di folder EXE, " +
                "pemuat Wine menolak memulai proses sebelum jendela game muncul.",
            evidence = "- Import statis: academyres.dll, levels.bin (Adventure Inlay memuat file .bin ini sebagai DLL), astrobaticsres.dll, beje2res.dll, resball.dll, igglepopres.dll, sdl_i.dll dan SDL.dll, parkerres.dll, " +
                "tennistitansres.dll, tradewinds2.dll dan gd204.dll dan bass.dll, domres.dll, joltresources.dll, wordjong.dll, bass.dll (Bejeweled 2, Hamsterball).\n" +
                "- Super WHATword memuat WhatResource.dll dan Tap a Jam memuat tapajamres.dll/Vorbis.dll secara dinamis.",
            signs = "Game tidak membuka jendela sama sekali. Log: 'err:module:import_dll Library X.dll (which is needed by ...) not found'.",
            steps = listOf(
                "Jalankan Pindai ZIP: semua DLL di atas dicek ukuran dan CRC-nya.",
                "Pastikan EXE dan DLL berada di folder yang sama (tidak ada folder tambahan akibat ekstraksi ganda).",
                "Untuk Adventure Inlay: levels.bin harus berukuran sama dengan referensi karena dimuat sebagai DLL.",
                "Jalankan game lewat jalur lengkap EXE-nya agar direktori kerja (working directory) otomatis folder game.",
                "Gunakan Analisis log untuk melihat nama DLL persis yang dikeluhkan."
            ),
            verify = "Jendela game muncul dan log tidak lagi menyebut import_dll.",
            ifFails = "Kirim baris 'import_dll' dari log."
        ),

        Issue(
            id = "PERF_GENERIC",
            title = "Lambat atau patah-patah",
            symptoms = setOf(Symptom.SLOW),
            confidence = "Saran UMUM; pengaruh tiap langkah pada perangkatmu: BELUM DIUJI.",
            cause = "Game 2D tahun 2004 sangat ringan, jadi kelambatan hampir selalu berasal dari pengaturan Winlator: pencatatan log yang aktif, wrapper grafis yang berat, layar virtual yang besar, atau preset Box64 yang terlalu konservatif.",
            evidence = "- Persyaratan asli game-game ini hanya 233-800 MHz dan 32-256 MB RAM (readme).\n" +
                "- Enable Wine debug, Save logs to file, dan Box64 Logs menulis log terus-menerus dan memperlambat.",
            signs = "Animasi tersendat, input terlambat, kipas/panas berlebih.",
            steps = listOf(
                "Matikan 'Enable Wine debug', 'Save logs to file', dan 'Box64 Logs' setelah selesai mengambil log.",
                "Turunkan Screen Size ke resolusi asli game (640x480 atau 800x600).",
                "Coba DDraw Wrapper atau DX Wrapper yang lebih ringan sesuai jenis game (lihat DDRAW_DISPLAY dan D3D8_RENDER).",
                "Box64 Preset: pindah ke preset yang lebih cepat bila stabil, atau kembalikan ke bawaan jika tadi diubah demi kestabilan.",
                "Tutup aplikasi latar belakang dan nonaktifkan penghemat baterai."
            ),
            verify = "Animasi mulus dan input langsung terasa.",
            ifFails = "Sertakan hasil Diagnostik perangkat dan pengaturan yang kamu pakai."
        )
    )

    private val byId: Map<String, Issue> = all.associateBy { it.id }

    fun get(id: String): Issue? = byId[id]
}
