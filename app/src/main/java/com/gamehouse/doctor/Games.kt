package com.gamehouse.doctor

/** Profil teknis 15 game GameHouse di pack referensi. Setiap angka berasal dari analisis file di ZIP (kecuali yang ditandai dugaan). */
object Games {

    private fun r(id: String, level: Level, why: String) = Risk(id, level, why)
    private fun integrity(why: String) = r("FILE_INTEGRITY", Level.RENDAH, why)
    private fun loader(why: String) = r("LOADER_DLL", Level.RENDAH, why)

    val all: List<GameProfile> = listOf(

        GameProfile(
            id = "academy_of_magic", name = "Academy of Magic", vendor = "Total Eclipse Studios (dijual GameHouse)", year = "2005",
            engine = "Delphi (ada kunci registri JEDI-VCL) yang memuat kontrol ActiveX; EXE terbungkus (seksi .garr), 11,9 MB.",
            requirements = "Windows 98/ME/2000/XP, 800 MHz, 128 MB RAM, DirectX 8.0 atau lebih baru.",
            dependencies = "academyres.dll (impor statis); ole32, oleaut32, urlmon, wininet, wsock32, winmm, comctl32, comdlg32, winspool.drv, version, shell32; folder data (5 file) dan dictionary (16 file).",
            baseline = "Tidak ada log uji referensi di ZIP. Dari tangkapan layarmu: AcademyofMagic.exe *32 berjalan di Task Manager (104,9 MB), lalu dialog 'OLE error 80040154.' muncul. " +
                "Artinya game sudah dimuat penuh dan gagal pada satu permintaan komponen COM, bukan gagal start.",
            regKeys = listOf(
                "HKCU\\Software\\JEDI-VCL\\TipsStartup",
                "HKLM\\Software\\GameHouse\\AcademyofMagic",
                "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Academy of Magic"
            ),
            dataDirs = emptyList(),
            setup = listOf(
                "Langkah wajib pertama: ambil log (Enable Wine debug + Save logs to file) lalu Analisis log untuk memastikan kelas COM yang hilang.",
                "Screen Size 800x600 sebagai titik awal (dugaan: game Delphi berbasis jendela). Grafis bawaan sudah cukup; game ini bukan 3D.",
                "Pasang 'Wine Gecko (For HTML Pages)' dari Start -> System Tools -> Installers bila panel HTML kosong.",
                "Jangan mengubah pengaturan grafis/audio dulu: error ini terjadi sebelum rendering, jadi tidak terkait driver grafis."
            ),
            risks = listOf(
                r("FLASH_ACTIVEX", Level.TINGGI, "Gejala persis cocok (OLE error 80040154 di aplikasi Delphi), EXE mengimpor OleRun, dan ada potongan type library Flash di dalam EXE. Flash adalah satu-satunya kontrol umum yang tidak terdaftar di container Winlator."),
                r("HTML_CONTROL", Level.SEDANG, "EXE mengimpor urlmon.dll dan wininet.dll; bila game menampilkan halaman web, Gecko dibutuhkan. Belum terbukti dipakai."),
                r("PACKED_EXE", Level.SEDANG, "Hanya 40 KB kode di dalam seksi .text dari file 11,9 MB; sisanya terkompresi oleh pembungkus."),
                r("REG_KEYS", Level.SEDANG, "Install.log mencatat tiga kunci registri; salah satunya HKCU\\Software\\JEDI-VCL\\TipsStartup (pengaturan tips saat mulai)."),
                r("ONLINE_CALLS", Level.RENDAH, "Mengimpor InternetOpenA, urlmon, dan wininet."),
                loader("Mengimpor academyres.dll secara statis."),
                integrity("Folder dictionary (16 file) berisi kamus kata; kehilangan satu file bisa membuat kata ditolak.")
            )
        ),

        GameProfile(
            id = "adventure_inlay", name = "Adventure Inlay", vendor = "GameHouse", year = "2004-2005 (EXE dibangun Januari 2005)",
            engine = "Mesin GameHouse sendiri dengan FMOD; EXE terbungkus (.garr); memuat levels.bin sebagai DLL.",
            requirements = "Readme tidak mencantumkan angka persyaratan; menyarankan memperbarui driver suara dan DirectX.",
            dependencies = "levels.bin (36 KB, diimpor sebagai DLL), inlayui.bin (10 MB, data antarmuka), msacm32, winmm (waveOut), ole32, shell32, advapi32.",
            baseline = "Tidak ada log uji referensi di ZIP.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\AdventureInlay", "HKLM\\Software\\GameHouse\\AdventureInlay"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 640x480 sebagai titik awal (dugaan era 2004); naikkan ke 800x600 bila jendela terpotong.",
                "DDraw Wrapper: 'CNC DDraw' bila layar hitam atau tidak mau tampil.",
                "Audio Driver: coba ganti ALSA/PulseAudio bila senyap."
            ),
            risks = listOf(
                r("LOADER_DLL", Level.TINGGI, "EXE mengimpor 'levels.bin' sebagai DLL. Jika file ini hilang atau berbeda, game tidak akan membuka jendela sama sekali."),
                r("AUDIO_WAVEOUT", Level.SEDANG, "Memakai FMOD lewat waveOut (waveOutOpen, waveOutWrite, msacm32)."),
                r("DDRAW_DISPLAY", Level.SEDANG, "Tidak mengimpor ddraw secara statis; kemungkinan memuatnya dinamis seperti mesin GameHouse lain (belum terbukti)."),
                r("PACKED_EXE", Level.SEDANG, "Seksi .garr terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse (HKCU dan HKLM)."),
                integrity("Hanya 5 file; inlayui.bin (10 MB) dan levels.bin harus utuh.")
            )
        ),

        GameProfile(
            id = "astrobatics", name = "Astrobatics", vendor = "KraiSoft Entertainment (GameHouse)", year = "2003-2004",
            engine = "Delphi/Borland (seksi CODE, DATA, BSS); EXE terbungkus (GARR).",
            requirements = "Windows 98/ME/2000/XP, 400 MHz, 32 MB RAM, grafis 16-bit, DirectX 6.0.",
            dependencies = "astrobaticsres.dll (impor statis), folder resource (12 file, 21 MB), SendBugReport.exe, ole32, oleaut32, wininet, winmm, version.",
            baseline = "Exception.Log dari uji referensi (28/7/2026): createfullscreendisplay 'Ok', kernel start 'Ok', sound effect true, midi false, lalu 'onactivate'/'ondeactivate' bergantian " +
                "selama sekitar 40 detik. Jadi game berjalan sehat pada PC referensi; 'midi false' tampaknya opsi, bukan kegagalan.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\Astrobatics", "HKCU\\Software\\KraiSoft Entertainment\\Astrobatics", "HKLM\\Software\\GameHouse\\Astrobatics"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 640x480 atau 800x600 (readme hanya meminta grafis 16-bit).",
                "DDraw Wrapper 'CNC DDraw' bila mode layar penuh gagal.",
                "Baca Exception.Log di folder game setelah mencoba: baris 'createfullscreendisplay' dan 'kernel start' menunjukkan sampai mana game berhasil."
            ),
            risks = listOf(
                r("DDRAW_DISPLAY", Level.SEDANG, "Log mencatat 'createfullscreendisplay' (pembuatan layar penuh DirectDraw); readme meminta DirectX 6 dan 16-bit."),
                r("FOCUS_LOSS", Level.SEDANG, "Log referensi menunjukkan onactivate/ondeactivate berulang."),
                r("ONLINE_CALLS", Level.SEDANG, "Mengimpor InternetOpenA dan InternetOpenUrlA."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Tiga kunci: GameHouse (HKCU, HKLM) dan KraiSoft Entertainment (HKCU)."),
                r("MIDI_MUSIC", Level.RENDAH, "Log mencatat 'midi: false'."),
                loader("Mengimpor astrobaticsres.dll secara statis."),
                integrity("Folder resource (12 file, 21 MB) berisi aset; Astrobatics.tpl (548 byte) ikut diperiksa.")
            )
        ),

        GameProfile(
            id = "bejeweled2", name = "Bejeweled 2", vendor = "PopCap Games (GameHouse)", year = "2000-2004 (EXE dibangun Oktober 2004)",
            engine = "Kerangka PopCap (2D DirectDraw/Direct3D 7) dengan BASS untuk audio; EXE terbungkus (.garr).",
            requirements = "Windows 98/ME/2000/XP, 350 MHz, 128 MB RAM, grafis 16-bit dan kartu suara, DirectX 7.0.",
            dependencies = "beje2res.dll dan bass.dll (impor statis), wsock32, ole32; 933 file di 9 subfolder (data, edata, images, music, properties, puzzles, sm_images, sounds, users); vhwb.dat (15 byte); properties/partner.xml.sig.",
            baseline = "Tidak ada log uji referensi. Folder users berisi dua file profil.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\Bejeweled2", "HKLM\\Software\\GameHouse\\Bejeweled2", "HKLM\\Software\\PopCap\\Bejeweled2"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 640x480 atau 800x600; mulai dengan 800x600.",
                "DDraw Wrapper 'CNC DDraw' (perbaikan paling umum untuk game PopCap lama bila layar hitam/berkedip).",
                "Bila ada opsi di dalam game (Options -> Windowed/Full Screen, 3D Acceleration), coba Windowed dan matikan akselerasi 3D lebih dulu."
            ),
            risks = listOf(
                r("DDRAW_DISPLAY", Level.TINGGI, "Kerangka PopCap 2004 memakai DirectDraw/Direct3D 7 dan meminta grafis 16-bit. Folder sm_images (144 file) tampak seperti aset versi kecil (dugaan)."),
                r("REG_KEYS", Level.SEDANG, "Ada kunci PopCap tambahan (HKLM\\Software\\PopCap\\Bejeweled2) selain kunci GameHouse; bisa menyimpan status registrasi."),
                r("PACKED_EXE", Level.SEDANG, "Seksi .garr terdeteksi."),
                r("AUDIO_WAVEOUT", Level.RENDAH, "Audio memakai bass.dll."),
                loader("Mengimpor beje2res.dll dan bass.dll secara statis."),
                integrity("933 file: kehilangan satu gambar/suara menyebabkan gagal saat memuat. partner.xml.sig (24 byte) adalah tanda tangan; jangan mengubah partner.xml.")
            )
        ),

        GameProfile(
            id = "hamsterball", name = "Hamsterball", vendor = "Raptisoft (GameHouse)", year = "2002-2004 (EXE dibangun Juni 2004)",
            engine = "Game 3D Direct3D 8 dengan DirectInput 8, DirectSound, dan BASS; EXE terbungkus (GARR).",
            requirements = "Windows 95/98/ME/2000/XP, 700 MHz, 32 MB RAM, grafis 16-bit dan kartu suara, DirectX 8.0.",
            dependencies = "resball.dll dan bass.dll (impor statis); d3d8, dsound, dinput8, ws2_32; folder Data, Fonts, Levels (98), Meshes (57), Music, Sounds (58), Textures (235); Jukebox.xml, RaceData.xml.",
            baseline = "Folder Levels dan Meshes berisi file *.cached, serta Data/TOURNAMENT.SAV (151 byte): tanda bahwa game pernah dijalankan di sumber ZIP (bukti tidak langsung, tidak tahu di sistem apa).",
            regKeys = listOf("HKCU\\Software\\GameHouse\\Hamsterball", "HKLM\\Software\\GameHouse\\Hamsterball"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 800x600 atau 640x480 (game 3D bebas resolusi; mulai rendah).",
                "DX Wrapper: coba WineD3D dulu, lalu DXVK 2.4.1 (memuat d3d8). Graphics Driver: Turnip untuk Adreno; Vortek/Zink/VirGL untuk GPU lain.",
                "Aktifkan 'Relative Mouse Movement' karena game mengambil alih mouse.",
                "Start -> System Tools -> Test Direct3D sebelum menjalankan game untuk memastikan Direct3D dasar berfungsi."
            ),
            risks = listOf(
                r("D3D8_RENDER", Level.TINGGI, "EXE mengimpor d3d8.dll dan memanggil Direct3DCreate8; 3D penuh dengan 235 tekstur dan 57 mesh."),
                r("MOUSE_INPUT", Level.TINGGI, "Readme menyebut kendali mouse normal hilang saat bermain; memakai DirectInput 8 (DirectInput8Create)."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse (HKCU dan HKLM)."),
                r("PERF_GENERIC", Level.SEDANG, "Satu-satunya game 3D di pack ini selain Tennis Titans dan Word Jolt (2D berbasis D3D)."),
                r("AUDIO_WAVEOUT", Level.RENDAH, "DirectSound dan BASS."),
                loader("Mengimpor resball.dll dan bass.dll secara statis."),
                integrity("469 file; Levels (98 file) dan Textures (235 file) harus lengkap.")
            )
        ),

        GameProfile(
            id = "iggle_pop", name = "Iggle Pop!", vendor = "Sprout Games (GameHouse)", year = "2004 (EXE dibangun Desember 2004)",
            engine = "Game 2D berbasis DirectDraw dengan arsip aset IgglePop.saf (6,6 MB); EXE terbungkus (GARR).",
            requirements = "Windows 95/98/ME/2000/XP, 500 MHz, 96 MB RAM, grafis 16-bit dan kartu suara, DirectX 7.0.",
            dependencies = "igglepopres.dll (impor statis); dsound, wininet, shlwapi, winmm, version; IgglePop.saf; folder splash (11 file).",
            baseline = "IgglePop.log dari uji referensi (28/7/2026 23:54): 'ddraw.dll loaded', 'using primary DirectDraw device', 'Compatible primary surface. Using ddraw blt', " +
                "'score: 8200 out of 8200', 'randomClip: 5 (musicLoop5)'. Jadi game berjalan penuh (sampai skor dan musik) pada PC referensi.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\IgglePop!", "HKLM\\Software\\GameHouse\\IgglePop"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 640x480 atau 800x600; DDraw Wrapper 'CNC DDraw'.",
                "Setelah mencoba, baca IgglePop.log: baris 'ddraw.dll loaded' dan 'Using ddraw blt' menandakan inisialisasi grafis berhasil."
            ),
            risks = listOf(
                r("DDRAW_DISPLAY", Level.SEDANG, "Log referensi menegaskan rendering lewat ddraw blt pada primary surface; ini jalur yang paling sensitif terhadap mode layar Winlator."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse; perhatikan nama berbeda 'IgglePop!' (HKCU) dan 'IgglePop' (HKLM)."),
                r("ONLINE_CALLS", Level.RENDAH, "Mengimpor InternetOpenA dan InternetOpenUrlA."),
                r("AUDIO_WAVEOUT", Level.RENDAH, "Memakai dsound.dll."),
                loader("Mengimpor igglepopres.dll secara statis."),
                integrity("IgglePop.saf (6,6 MB) berisi hampir semua aset; wajib utuh.")
            )
        ),

        GameProfile(
            id = "incredible_ink", name = "Incredible Ink", vendor = "GameHouse", year = "2004 (EXE dibangun November 2004)",
            engine = "SDL 1.2 (SDL.dll + sdl_i.dll) dengan FMOD lewat waveOut; aset di IncredibleInk.bin (4,1 MB); EXE terbungkus (GARR).",
            requirements = "Readme tidak mencantumkan angka; menyarankan memperbarui driver suara dan DirectX.",
            dependencies = "SDL.dll dan sdl_i.dll (impor statis); msacm32, winmm (waveOut), ole32; folder config (13 file).",
            baseline = "Tidak ada log uji referensi di ZIP.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\IncredibleInk", "HKLM\\Software\\GameHouse\\IncredibleInk"),
            dataDirs = emptyList(),
            setup = listOf(
                "Environment Variables: SDL_VIDEODRIVER=windib sebagai percobaan pertama.",
                "Screen Size 800x600 (dugaan); DDraw Wrapper 'CNC DDraw' bila windib membuat gambar kecil.",
                "Audio Driver: coba ganti ALSA/PulseAudio; bila senyap tambahkan SDL_AUDIODRIVER (waveout atau dsound)."
            ),
            risks = listOf(
                r("SDL12_VIDEO", Level.TINGGI, "Mengimpor SDL.dll dan sdl_i.dll; SDL 1.2 memilih driver video directx secara bawaan di Windows."),
                r("AUDIO_WAVEOUT", Level.SEDANG, "FMOD lewat waveOut (waveOutOpen, waveOutWrite, msacm32)."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse."),
                r("LOADER_DLL", Level.SEDANG, "Dua DLL statis (SDL.dll, sdl_i.dll); keduanya wajib ada di folder EXE."),
                integrity("IncredibleInk.bin (4,1 MB) dan folder config (13 file) harus utuh.")
            )
        ),

        GameProfile(
            id = "inspector_parker", name = "Inspector Parker", vendor = "Oberon Media (GameHouse)", year = "2004 (proyektor dibangun Desember 2002)",
            engine = "Proyektor Macromedia Director: Parker.exe + Parker.dcr + cast x*.cct + 9 Xtra; EXE terbungkus (GARR).",
            requirements = "Windows 95/98/ME/2000/XP, 233 MHz, 32 MB RAM, grafis dan suara 16-bit.",
            dependencies = "parkerres.dll (impor statis); Xtras: DirectSound, Font Asset, Font Xtra, SWADCmpr, Sound Control, Text Asset, TextXtra, budapi, resolution; xAssets/xObjects/xRooms/xScreens.cct; Parker.bmp.",
            baseline = "Tidak ada log uji referensi. Install.log memuat anomali nyata: folder 'THIS NEEDS TO BE FIX' di AppData\\Roaming (placeholder installer yang gagal menyelesaikan variabel).",
            regKeys = listOf("HKLM\\Software\\GameHouse\\Parker"),
            dataDirs = listOf("C:\\ProgramData\\Oberon Media\\Inspector Parker", "C:\\users\\xuser\\AppData\\Roaming\\THIS NEEDS TO BE FIX"),
            setup = listOf(
                "Screen Size 640x480 (resolusi asli proyektor 2002; dugaan).",
                "DDraw Wrapper 'CNC DDraw'.",
                "Buat folder C:\\ProgramData\\Oberon Media\\Inspector Parker di container bila belum ada, lalu uji.",
                "Jangan memindahkan folder Xtras dari samping Parker.exe."
            ),
            risks = listOf(
                r("DIRECTOR_RUNTIME", Level.TINGGI, "Proyektor Director dengan 9 Xtra, termasuk budapi.x32 (Buddy API) dan resolution.x32 (dari namanya mengatur resolusi; dugaan)."),
                r("DATA_DIRS", Level.TINGGI, "Install.log membuat dua folder data, salah satunya bernama placeholder 'THIS NEEDS TO BE FIX'."),
                r("DDRAW_DISPLAY", Level.SEDANG, "Xtra resolution.x32 menunjukkan kemungkinan perubahan resolusi layar."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Hanya satu kunci: HKLM\\Software\\GameHouse\\Parker."),
                loader("Mengimpor parkerres.dll secara statis."),
                integrity("Folder Xtras (9 file) dan empat cast .cct harus lengkap.")
            )
        ),

        GameProfile(
            id = "super_whatword", name = "Super WHATword", vendor = "GameHouse", year = "2001-2002 (EXE dibangun Desember 2002)",
            engine = "Mesin GameHouse lama dengan FMOD; EXE dilindungi protector pola lain (tujuh seksi, empat bernama kosong, ada .adata).",
            requirements = "Readme: memakai kartu suara dan video secara intensif; menyarankan DirectX terbaru.",
            dependencies = "WhatResource.dll (872 KB, dimuat dinamis), whattree.bin (19 KB, struktur kata); msacm32, winmm, ole32, shell32.",
            baseline = "Tidak ada log uji referensi di ZIP.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\WHATword", "HKLM\\Software\\GameHouse\\WHATword"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 640x480 atau 800x600.",
                "Box64 Preset: stabilitas lebih dulu (lihat PACKED_EXE); game ringan sehingga aman diperlambat.",
                "Audio Driver: ganti ALSA/PulseAudio bila senyap."
            ),
            risks = listOf(
                r("PACKED_EXE", Level.TINGGI, "Pola protector berbeda dari pembungkus GARR (seksi tanpa nama dan .adata); tipe protector seperti ini lazim sensitif terhadap emulasi."),
                r("AUDIO_WAVEOUT", Level.SEDANG, "FMOD dan waveOut (msacm32)."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse."),
                loader("WhatResource.dll dimuat dinamis lewat LoadLibraryA; harus ada di folder EXE."),
                integrity("Hanya 5 file; whattree.bin (19 KB) dan WhatResource.dll wajib utuh.")
            )
        ),

        GameProfile(
            id = "tap_a_jam", name = "Tap a Jam", vendor = "GameHouse", year = "2001-2003 (EXE dibangun Oktober 2003)",
            engine = "Mesin GameHouse dengan FMOD dan Vorbis; EXE dilindungi protector pola .adata.",
            requirements = "Readme: menyarankan memperbarui driver suara dan DirectX.",
            dependencies = "tapajamres.dll (1,9 MB), Vorbis.dll (149 KB); folder songs (39 file), Saved Jams; msacm32, winmm, wininet, comdlg32.",
            baseline = "Folder Saved Jams berisi satu file (kemungkinan hasil sesi sebelumnya). Tidak ada log referensi lain.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\Tap a Jam", "HKLM\\Software\\GameHouse\\TapaJam"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size terkecil yang nyaman (640x480 atau 800x600) agar CPU lega untuk audio.",
                "Audio Driver: uji ALSA dan PulseAudio; game ritme sangat peka terhadap latensi.",
                "Box64 Preset stabilitas dulu; bila lancar, coba preset lebih cepat."
            ),
            risks = listOf(
                r("AUDIO_WAVEOUT", Level.TINGGI, "Game ritme dengan FMOD/waveOut dan Vorbis.dll; penundaan kecil pun terasa."),
                r("PACKED_EXE", Level.TINGGI, "Protector pola .adata (bukan GARR)."),
                r("DATA_DIRS", Level.SEDANG, "Menulis lagu buatan pemain ke folder Saved Jams di folder game."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse (nama berspasi 'Tap a Jam' di HKCU)."),
                r("ONLINE_CALLS", Level.RENDAH, "Mengimpor wininet."),
                loader("Memuat tapajamres.dll dan Vorbis.dll."),
                integrity("39 file lagu di folder songs dan Vorbis.dll harus utuh.")
            )
        ),

        GameProfile(
            id = "tennis_titans", name = "Tennis Titans", vendor = "Skunk Studios (GameHouse)", year = "2005 (EXE dibangun September 2004)",
            engine = "Macromedia Director MX 2004 dengan Shockwave 3D; EXE terbungkus (.garr), 4,3 MB.",
            requirements = "Windows 98/ME/2000/XP, 750 MHz, 256 MB RAM, DirectX 7.0 atau lebih baru, atau OpenGL.",
            dependencies = "tennistitansres.dll (impor statis); components: code.cct, models.cct (4,6 MB), online.cct, sound.cct (2,4 MB), textures.cct (7,5 MB); images: loader.swf, moregreatgames.swf.",
            baseline = "Tidak ada log uji referensi di ZIP.",
            regKeys = listOf("HKLM\\Software\\GameHouse\\Tennis Titans", "HKLM\\Software\\Skunkstudios\\t23rtns3"),
            dataDirs = listOf("C:\\users\\xuser\\AppData\\Roaming\\Macromedia\\Director MX 2004\\Tennis Titans"),
            setup = listOf(
                "Screen Size 800x600 sebagai titik awal.",
                "DX Wrapper 'WineD3D' dulu; bila layar hitam saat masuk pertandingan, coba 'DXVK'.",
                "Pastikan folder AppData\\Roaming\\Macromedia\\Director MX 2004\\Tennis Titans ada dan bisa ditulis."
            ),
            risks = listOf(
                r("DIRECTOR_RUNTIME", Level.TINGGI, "Shockwave 3D memilih sendiri renderer (OpenGL/DirectX 7/software); readme menyebut 'DirectX 7.0 atau OpenGL'."),
                r("DDRAW_DISPLAY", Level.SEDANG, "Mode layar DirectX 7 untuk renderer Director."),
                r("PERF_GENERIC", Level.SEDANG, "Game 3D dengan model dan tekstur total sekitar 12 MB; persyaratan asli 750 MHz dan 256 MB."),
                r("DATA_DIRS", Level.SEDANG, "Install.log membuat folder data di AppData\\Roaming\\Macromedia\\Director MX 2004."),
                r("PACKED_EXE", Level.SEDANG, "Seksi .garr terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Kunci tambahan Skunkstudios\\t23rtns3 selain kunci GameHouse (kemungkinan penyimpan status lisensi pengembang)."),
                loader("Mengimpor tennistitansres.dll secara statis."),
                integrity("Lima cast .cct di components wajib utuh; dua file .swf di images dipakai antarmuka.")
            )
        ),

        GameProfile(
            id = "tradewinds2", name = "Tradewinds 2", vendor = "Sandlot Games (GameHouse)", year = "2005 (EXE dibangun Juni 2005)",
            engine = "Mesin 2D dengan pustaka grafis GD (gd204.dll) dan BASS; hanya memakai GDI/User32 (tanpa impor DirectDraw); EXE terbungkus (.garr).",
            requirements = "Readme tidak tersedia di ZIP untuk game ini (tidak ada readme.txt).",
            dependencies = "tradewinds2.dll, gd204.dll, bass.dll (impor statis); TW2_00.data (1,8 MB), TW2_01.data (9,8 MB); settings.dat (44 byte), default_settings.dat (20 byte); folder help (25 file HTML).",
            baseline = "settings.dat (44 byte) dan default_settings.dat (20 byte) ada dan tidak kosong; ukurannya kecil normal untuk berkas pengaturan.",
            regKeys = listOf("HKLM\\Software\\GameHouse\\Tradewinds2"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 800x600 atau 1024x768 (game 2D GDI, bebas resolusi; mulai 800x600).",
                "Karena memakai GDI, pengaturan DirectDraw/Direct3D tidak berpengaruh; fokus ke Screen Size dan Box64 Preset.",
                "Pastikan folder game bisa ditulis agar settings.dat dapat diperbarui."
            ),
            risks = listOf(
                r("FILE_INTEGRITY", Level.SEDANG, "TW2_00.data dan TW2_01.data (total 11,7 MB) adalah arsip data utama; wajib utuh."),
                r("LOADER_DLL", Level.SEDANG, "Tiga DLL statis (tradewinds2.dll, gd204.dll, bass.dll)."),
                r("DATA_DIRS", Level.SEDANG, "settings.dat diperbarui saat bermain; folder game harus bisa ditulis."),
                r("PACKED_EXE", Level.SEDANG, "Seksi .garr terdeteksi."),
                r("PERF_GENERIC", Level.SEDANG, "Rendering GDI/perangkat lunak lewat X11 bisa lebih lambat dari DirectDraw."),
                r("HTML_CONTROL", Level.RENDAH, "Tombol Help kemungkinan membuka halaman HTML di folder help lewat ShellExecuteExA; butuh browser/Gecko."),
                r("REG_KEYS", Level.SEDANG, "Satu kunci: HKLM\\Software\\GameHouse\\Tradewinds2.")
            )
        ),

        GameProfile(
            id = "ultimate_dominoes", name = "Ultimate Dominoes", vendor = "Antidote Corp (GameHouse)", year = "2004 (mesin dibangun April 2001)",
            engine = "Mesin 2D lama (EXE 6,3 MB) dengan mode layar DirectDraw; EXE terbungkus (GARR).",
            requirements = "Windows 95/98/ME/2000/XP, 32 MB RAM, grafis dan suara 16-bit, resolusi layar 800x600.",
            dependencies = "domres.dll (impor statis); LoadingScreen.bmp (1,4 MB), gh_link.bmp; kernel32, user32, gdi32, advapi32.",
            baseline = "errors.txt dari uji referensi berisi 'SetResolution Failed 0'. File itu berasal dari PC Windows, jadi kegagalan mengatur resolusi sudah muncul di luar Winlator.",
            regKeys = listOf("HKCU\\Software\\GameHouse\\Dominoes", "HKLM\\Software\\GameHouse\\Dominoes"),
            dataDirs = emptyList(),
            setup = listOf(
                "Screen Size 800x600 TEPAT (readme meminta resolusi 800x600).",
                "DDraw Wrapper 'CNC DDraw' (meniru perubahan mode 16-bit).",
                "Aktifkan 'Force Fullscreen' bila gambar tampil kecil.",
                "Baca errors.txt setelah setiap percobaan: baris 'SetResolution Failed' menunjukkan pengaturan mode layar belum berhasil."
            ),
            risks = listOf(
                r("DDRAW_DISPLAY", Level.TINGGI, "Bukti langsung: errors.txt mencatat 'SetResolution Failed 0'. Resolusi dan kedalaman warna yang diminta game tidak tersedia."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Dua kunci GameHouse (nama 'Dominoes')."),
                loader("Mengimpor domres.dll secara statis."),
                integrity("EXE 6,3 MB dan domres.dll wajib utuh.")
            )
        ),

        GameProfile(
            id = "word_jolt", name = "Word Jolt", vendor = "GreenLight Games (GameHouse)", year = "2004 (EXE dibangun Januari 2004)",
            engine = "Game kata dengan rendering Direct3D 8 (Direct3DCreate8), DirectSound, dan msacm32; EXE terbungkus (GARR).",
            requirements = "Windows 95/98/ME/2000/XP, 64 MB RAM, grafis 16-bit dan kartu suara.",
            dependencies = "joltresources.dll (impor statis); d3d8, dsound, msacm32, msvcrt; folder data (1 file 0,8 MB), images (405 file), sound (18 file); settings.ini.",
            baseline = "settings.ini mencatat uji Direct3D pada PC Windows: Driver nvd3dum.dll, NVIDIA GeForce GTX 480, GetAdapterIdentifier/GetDeviceCaps/ChooseTextureFormat 'Succeeded', WindowedOK=1, FullscreenOK=1, " +
                "format X8R8G8B8/A8R8G8B8. Jadi game berjalan sehat di PC referensi, tetapi nilai itu spesifik GPU tersebut.",
            regKeys = listOf("HKLM\\Software\\GameHouse\\WordJolt"),
            dataDirs = emptyList(),
            setup = listOf(
                "Jalankan Start -> System Tools -> Test Direct3D lebih dulu.",
                "DX Wrapper 'WineD3D' dulu, lalu 'DXVK' bila hitam; Graphics Driver sesuai GPU (Turnip untuk Adreno).",
                "Bila layar hitam walau Direct3D normal: cadangkan lalu hapus settings.ini agar game mendeteksi ulang GPU (skor ikut hilang).",
                "Screen Size 800x600 (dugaan)."
            ),
            risks = listOf(
                r("D3D8_RENDER", Level.TINGGI, "Mengimpor d3d8.dll dan Direct3DCreate8 untuk menggambar permainan kata."),
                r("STALE_CONFIG", Level.SEDANG, "settings.ini membawa hasil deteksi GTX 480 dari PC Windows."),
                r("PACKED_EXE", Level.SEDANG, "Seksi GARR terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Satu kunci: HKLM\\Software\\GameHouse\\WordJolt."),
                r("AUDIO_WAVEOUT", Level.RENDAH, "dsound dan msacm32; settings.ini mencatat music=1, sound=1."),
                loader("Mengimpor joltresources.dll secara statis."),
                integrity("images (405 file) dan sound (18 file) wajib lengkap.")
            )
        ),

        GameProfile(
            id = "wordjong", name = "WordJong To Go", vendor = "Gameblend Studios (GameHouse)", year = "2005 (EXE dibangun Mei 2005)",
            engine = "Game kata 2D dengan DirectSound dan aset animasi .a2d; menyertakan msvcp60.dll (runtime Visual C++ 6); EXE terbungkus (.garr).",
            requirements = "Windows 98/ME/2000/XP, 350 MHz, 128 MB RAM, grafis dan suara 16-bit, DirectX 6.0.",
            dependencies = "wordjong.dll (impor statis), msvcp60.dll (392 KB, di folder game), msvcrt, dsound, comdlg32, version; folder res (569 file).",
            baseline = "Tidak ada log uji referensi di ZIP.",
            regKeys = listOf("HKLM\\Software\\GameHouse\\WordJong"),
            dataDirs = listOf("C:\\users\\xuser\\AppData\\Roaming\\GameBlend\\WordJong"),
            setup = listOf(
                "Screen Size 640x480 atau 800x600.",
                "Buat folder C:\\users\\xuser\\AppData\\Roaming\\GameBlend\\WordJong bila belum ada.",
                "msvcp60.dll di folder game akan dipakai lebih dulu daripada versi bawaan Wine; biarkan apa adanya."
            ),
            risks = listOf(
                r("DATA_DIRS", Level.SEDANG, "Install.log membuat folder AppData\\Roaming\\GameBlend\\WordJong untuk data pengguna."),
                r("PACKED_EXE", Level.SEDANG, "Seksi .garr terdeteksi."),
                r("REG_KEYS", Level.SEDANG, "Satu kunci: HKLM\\Software\\GameHouse\\WordJong."),
                r("LOADER_DLL", Level.SEDANG, "Dua DLL di folder game (wordjong.dll dan msvcp60.dll) harus cocok dengan referensi."),
                r("AUDIO_WAVEOUT", Level.RENDAH, "Memakai dsound.dll."),
                integrity("Folder res berisi 569 file aset (gambar, suara, animasi .a2d); jumlahnya diperiksa persis.")
            )
        )
    )

    private val byId = all.associateBy { it.id }
    fun get(id: String): GameProfile? = byId[id]
}
