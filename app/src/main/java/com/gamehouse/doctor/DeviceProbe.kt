package com.gamehouse.doctor

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.StatFs

/** Mengumpulkan fakta nyata tentang perangkat dan keberadaan Winlator. Tidak menjalankan game. */
object DeviceProbe {

    const val WINLATOR_PACKAGE = "com.winlator"

    fun winlatorInfo(ctx: Context): String? = try {
        val pi = ctx.packageManager.getPackageInfo(WINLATOR_PACKAGE, 0)
        val code = if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode.toString() else "?"
        "versi ${pi.versionName} (kode $code)"
    } catch (e: PackageManager.NameNotFoundException) {
        null
    } catch (e: Exception) {
        "tidak bisa dibaca: ${e.javaClass.simpleName}"
    }

    fun report(ctx: Context): String {
        val pm = ctx.packageManager
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val glEs = am.deviceConfigurationInfo.reqGlEsVersion
        val vulkan = try {
            pm.systemAvailableFeatures.firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }?.version
        } catch (e: Exception) {
            null
        }
        val free = try {
            StatFs(ctx.filesDir.path).availableBytes
        } catch (e: Exception) {
            -1L
        }
        val abis = Build.SUPPORTED_ABIS.toList()
        val soc = if (Build.VERSION.SDK_INT >= 31) "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}" else Build.HARDWARE

        return buildString {
            appendLine("DIAGNOSTIK PERANGKAT")
            appendLine("========================================")
            appendLine("PERANGKAT")
            appendLine("  Model      : ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("  Chipset    : $soc")
            appendLine("  Android    : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("  CPU ABI    : ${abis.joinToString()}")
            appendLine("  RAM total  : ${formatSize(mem.totalMem)}")
            appendLine("  Ruang bebas: ${if (free >= 0) formatSize(free) else "tidak diketahui"}")
            appendLine("  OpenGL ES  : ${glEs ushr 16}.${glEs and 0xFFFF}")
            appendLine()
            appendLine("PEMERIKSAAN UNTUK WINLATOR 11.2")
            CompatCheck.evaluate(abis, Build.VERSION.SDK_INT, glEs, vulkan).forEach { c ->
                val tag = when (c.status) {
                    Status.OK -> "[OK]    "
                    Status.FAIL -> "[GAGAL] "
                    Status.INFO -> "[INFO]  "
                }
                appendLine("  $tag${c.label}: ${c.detail}")
            }
            appendLine()
            appendLine("WINLATOR")
            val w = winlatorInfo(ctx)
            appendLine(if (w != null) "  Terpasang: ya, $w" else "  Terpasang: TIDAK terdeteksi (com.winlator)")
            appendLine()
            appendLine("PETUNJUK PEMILIHAN DRIVER GRAFIS (dari daftar driver di APK Winlator 11.2)")
            appendLine("  Turnip 26.2.0 untuk GPU Adreno; Vortek 2.1, Zink 22.2.5, VirGL 23.1.9, dan Gladio 1.1 untuk GPU lain.")
            appendLine("  Chipset di atas membantu menentukan GPU; Start -> System Tools -> GPUInfo di Winlator menampilkan GPU yang terdeteksi Wine.")
            appendLine()
            append("Catatan: ini hanya memeriksa perangkat. Ini BUKAN bukti bahwa game bisa berjalan.")
        }
    }
}
