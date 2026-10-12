package com.gamehouse.doctor

// DIHASILKAN OTOMATIS dari _GameHouse_Games_Collection.zip (referensi). Jangan diedit manual.

data class FileSpec(val name: String, val size: Long, val crc: Long, val volatile: Boolean)
data class DirSpec(val name: String, val files: Int, val bytes: Long, val volatile: Boolean)
data class GameManifest(
    val id: String, val folder: String, val exe: String,
    val files: List<FileSpec>, val dirs: List<DirSpec>,
    val totalFiles: Int, val totalBytes: Long
)

object GameCatalog {
    val all: List<GameManifest> = listOf(
        GameManifest("academy_of_magic", "Academy of Magic", "AcademyofMagic.exe",
            files = listOf(
                FileSpec("AcademyofMagic.exe", 12189626L, 261364516L, false),
                FileSpec("academyres.dll", 37376L, 2377342485L, false),
                FileSpec("Install.log", 705L, 866792998L, true),
                FileSpec("readme.txt", 3040L, 1858091617L, false),
            ),
            dirs = listOf(
                DirSpec("data", 5, 4912846L, false),
                DirSpec("dictionary", 16, 2939606L, false),
            ),
            totalFiles = 25, totalBytes = 20083199L
        ),
        GameManifest("adventure_inlay", "Adventure Inlay", "AdventureInlay.exe",
            files = listOf(
                FileSpec("AdventureInlay.exe", 693982L, 4281779976L, false),
                FileSpec("inlayui.bin", 10605733L, 965358815L, false),
                FileSpec("Install.log", 704L, 1457620848L, true),
                FileSpec("levels.bin", 36864L, 2363417209L, false),
                FileSpec("readme.txt", 3646L, 917310157L, false),
            ),
            dirs = listOf(
            ),
            totalFiles = 5, totalBytes = 11340929L
        ),
        GameManifest("astrobatics", "Astrobatics", "Astrobatics.exe",
            files = listOf(
                FileSpec("Astrobatics.exe", 704081L, 2139295060L, false),
                FileSpec("astrobaticsres.dll", 25088L, 4184720205L, false),
                FileSpec("Exception.Log", 1245L, 1017599923L, true),
                FileSpec("Install.log", 750L, 1029532941L, true),
                FileSpec("readme.txt", 2563L, 1898321768L, false),
                FileSpec("SendBugReport.exe", 114688L, 3455386409L, false),
            ),
            dirs = listOf(
                DirSpec("resource", 12, 22346711L, false),
            ),
            totalFiles = 18, totalBytes = 23195126L
        ),
        GameManifest("bejeweled2", "Bejeweled 2", "Bejeweled2.exe",
            files = listOf(
                FileSpec("bass.dll", 97336L, 1141092147L, false),
                FileSpec("beje2res.dll", 36352L, 2342219112L, false),
                FileSpec("Bejeweled2.exe", 1677315L, 1124041983L, false),
                FileSpec("Install.log", 731L, 3176585287L, true),
                FileSpec("readme.txt", 7862L, 360687865L, false),
                FileSpec("vhwb.dat", 15L, 2661325951L, false),
            ),
            dirs = listOf(
                DirSpec("data", 52, 443194L, false),
                DirSpec("edata", 8, 13382L, false),
                DirSpec("images", 373, 5377201L, false),
                DirSpec("music", 1, 437484L, false),
                DirSpec("properties", 7, 30020L, false),
                DirSpec("puzzles", 241, 1176478L, false),
                DirSpec("sm_images", 144, 15925524L, false),
                DirSpec("sounds", 99, 11051103L, false),
                DirSpec("users", 2, 17169L, true),
            ),
            totalFiles = 933, totalBytes = 36291166L
        ),
        GameManifest("hamsterball", "Hamsterball", "Hamsterball.exe",
            files = listOf(
                FileSpec("bass.dll", 97336L, 2438643430L, false),
                FileSpec("Hamsterball.exe", 1443813L, 44462960L, false),
                FileSpec("Install.log", 678L, 1065531689L, true),
                FileSpec("Jukebox.xml", 2828L, 3917011383L, true),
                FileSpec("RaceData.xml", 1929L, 969865398L, true),
                FileSpec("readme.txt", 17947L, 1772048111L, false),
                FileSpec("resball.dll", 25088L, 2321839487L, false),
            ),
            dirs = listOf(
                DirSpec("Data", 2, 1451L, true),
                DirSpec("Fonts", 11, 74076L, false),
                DirSpec("Levels", 98, 13650458L, false),
                DirSpec("Meshes", 57, 521467L, false),
                DirSpec("Music", 1, 193908L, false),
                DirSpec("Sounds", 58, 728043L, false),
                DirSpec("Textures", 235, 2292002L, false),
            ),
            totalFiles = 469, totalBytes = 19051024L
        ),
        GameManifest("iggle_pop", "Iggle Pop!", "IgglePop.exe",
            files = listOf(
                FileSpec("IgglePop.exe", 1361913L, 3763470513L, false),
                FileSpec("IgglePop.log", 219L, 1269977627L, true),
                FileSpec("IgglePop.saf", 6766425L, 3982802849L, false),
                FileSpec("igglepopres.dll", 26112L, 481090010L, false),
                FileSpec("Install.log", 668L, 2111579880L, true),
                FileSpec("readme.txt", 3190L, 1553504229L, false),
            ),
            dirs = listOf(
                DirSpec("splash", 11, 119488L, false),
            ),
            totalFiles = 17, totalBytes = 8278015L
        ),
        GameManifest("incredible_ink", "Incredible Ink", "IncredibleInk.exe",
            files = listOf(
                FileSpec("IncredibleInk.bin", 4270568L, 2125765481L, false),
                FileSpec("IncredibleInk.exe", 706303L, 2472815714L, false),
                FileSpec("Install.log", 697L, 861181298L, true),
                FileSpec("readme.txt", 3680L, 3193321865L, false),
                FileSpec("SDL.dll", 229376L, 833183860L, false),
                FileSpec("sdl_i.dll", 26112L, 3609040639L, false),
            ),
            dirs = listOf(
                DirSpec("config", 13, 1663307L, false),
            ),
            totalFiles = 19, totalBytes = 6900043L
        ),
        GameManifest("inspector_parker", "Inspector Parker", "Parker.exe",
            files = listOf(
                FileSpec("Install.log", 862L, 2777089316L, true),
                FileSpec("Parker.bmp", 41428L, 2023914616L, false),
                FileSpec("Parker.dcr", 815270L, 1772196874L, false),
                FileSpec("Parker.exe", 2189151L, 453075991L, false),
                FileSpec("parkerres.dll", 25088L, 2731791573L, false),
                FileSpec("readme.txt", 5834L, 444358322L, false),
                FileSpec("xAssets.cct", 1077915L, 937250773L, false),
                FileSpec("xObjects.cct", 456440L, 303329144L, false),
                FileSpec("xRooms.cct", 1218393L, 2954579993L, false),
                FileSpec("xScreens.cct", 396653L, 1674806318L, false),
            ),
            dirs = listOf(
                DirSpec("Xtras", 9, 1302528L, false),
            ),
            totalFiles = 19, totalBytes = 7529562L
        ),
        GameManifest("super_whatword", "Super WHATword", "WHATword.exe",
            files = listOf(
                FileSpec("Install.log", 687L, 1512450317L, true),
                FileSpec("readme.txt", 2304L, 2715043075L, false),
                FileSpec("WhatResource.dll", 892928L, 916397930L, false),
                FileSpec("whattree.bin", 20199L, 1069539348L, false),
                FileSpec("WHATword.exe", 318464L, 3458275715L, false),
            ),
            dirs = listOf(
            ),
            totalFiles = 5, totalBytes = 1234582L
        ),
        GameManifest("tap_a_jam", "Tap a Jam", "TapaJam.exe",
            files = listOf(
                FileSpec("HolidayMusic ReadMe.txt", 28L, 2937223105L, false),
                FileSpec("Install.log", 662L, 1237835975L, true),
                FileSpec("readme.txt", 2963L, 1749143494L, false),
                FileSpec("TapaJam.exe", 388096L, 701231639L, false),
                FileSpec("tapajamres.dll", 1945600L, 2639059934L, false),
                FileSpec("Vorbis.dll", 153088L, 961376391L, false),
                FileSpec("vorbis.txt", 1577L, 1473519849L, false),
            ),
            dirs = listOf(
                DirSpec("Saved Jams", 1, 47454L, true),
                DirSpec("songs", 39, 6405547L, false),
            ),
            totalFiles = 47, totalBytes = 8945015L
        ),
        GameManifest("tennis_titans", "Tennis Titans", "Tennis Titans.exe",
            files = listOf(
                FileSpec("Install.log", 898L, 593559533L, true),
                FileSpec("readme.txt", 3322L, 666125578L, false),
                FileSpec("Tennis Titans.exe", 4445862L, 953468698L, false),
                FileSpec("tennistitansres.dll", 36864L, 2096826818L, false),
            ),
            dirs = listOf(
                DirSpec("components", 5, 15357532L, false),
                DirSpec("images", 2, 55695L, false),
            ),
            totalFiles = 11, totalBytes = 19900173L
        ),
        GameManifest("tradewinds2", "Tradewinds 2", "Tradewinds2.exe",
            files = listOf(
                FileSpec("bass.dll", 95288L, 4079819888L, false),
                FileSpec("default_settings.dat", 20L, 1161808697L, false),
                FileSpec("gd204.dll", 409600L, 1503758943L, false),
                FileSpec("Install.log", 624L, 3229994204L, true),
                FileSpec("settings.dat", 44L, 2112523272L, true),
                FileSpec("Tradewinds2.dll", 37376L, 4065509568L, false),
                FileSpec("Tradewinds2.exe", 591534L, 647545138L, false),
                FileSpec("TW2_00.data", 1929197L, 3881192564L, false),
                FileSpec("TW2_01.data", 10312540L, 2948637823L, false),
            ),
            dirs = listOf(
                DirSpec("help", 25, 175854L, false),
            ),
            totalFiles = 34, totalBytes = 13552077L
        ),
        GameManifest("ultimate_dominoes", "Ultimate Dominoes", "Dominoes.exe",
            files = listOf(
                FileSpec("Dominoes.exe", 6489857L, 4116931933L, false),
                FileSpec("domres.dll", 24064L, 195825738L, false),
                FileSpec("errors.txt", 24L, 557754667L, true),
                FileSpec("gh_link.bmp", 26712L, 1207985751L, false),
                FileSpec("Install.log", 702L, 3994257590L, true),
                FileSpec("LoadingScreen.bmp", 1440056L, 669876622L, false),
                FileSpec("readme.txt", 2256L, 2437441761L, false),
            ),
            dirs = listOf(
            ),
            totalFiles = 7, totalBytes = 7983671L
        ),
        GameManifest("word_jolt", "Word Jolt", "WordJolt.exe",
            files = listOf(
                FileSpec("Install.log", 606L, 3706624792L, true),
                FileSpec("joltresources.dll", 25600L, 3842113392L, false),
                FileSpec("readme.txt", 2436L, 648404740L, false),
                FileSpec("settings.ini", 2053L, 2220690040L, true),
                FileSpec("WordJolt.exe", 391143L, 583091048L, false),
            ),
            dirs = listOf(
                DirSpec("data", 1, 798343L, false),
                DirSpec("images", 405, 2837292L, false),
                DirSpec("sound", 18, 1101692L, false),
            ),
            totalFiles = 429, totalBytes = 5159165L
        ),
        GameManifest("wordjong", "WordJong To Go", "WordJong.exe",
            files = listOf(
                FileSpec("Install.log", 745L, 3726809259L, true),
                FileSpec("msvcp60.dll", 401462L, 1719507572L, false),
                FileSpec("readme.txt", 4850L, 689906958L, false),
                FileSpec("WordJong.dll", 36864L, 1978401588L, false),
                FileSpec("WordJong.exe", 726776L, 2847840292L, false),
            ),
            dirs = listOf(
                DirSpec("res", 569, 9161565L, false),
            ),
            totalFiles = 574, totalBytes = 10332262L
        ),
    )

    fun byId(id: String): GameManifest? = all.firstOrNull { it.id == id }
}
