package com.gamehouse.doctor

enum class Status { OK, FAIL, INFO }

data class Check(val label: String, val status: Status, val detail: String)

/**
 * Pemeriksaan kompatibilitas perangkat terhadap Winlator 11.2, berdasarkan hasil analisis APK-nya:
 * hanya library arm64-v8a, minSdk 26, OpenGL ES 2.0 wajib. Fungsi murni, bisa diuji di JVM.
 */
object CompatCheck {

    fun evaluate(abis: List<String>, sdkInt: Int, glEsVersion: Int, vulkanVersion: Int?): List<Check> {
        val out = ArrayList<Check>()

        out += if ("arm64-v8a" in abis) {
            Check("CPU ARM 64-bit", Status.OK, "arm64-v8a tersedia")
        } else {
            Check(
                "CPU ARM 64-bit", Status.FAIL,
                "Winlator 11.2 hanya memuat library arm64-v8a; perangkat ini: ${abis.joinToString().ifEmpty { "(tidak diketahui)" }}"
            )
        }

        out += if (sdkInt >= 26) {
            Check("Versi Android", Status.OK, "API $sdkInt (minimal 26)")
        } else {
            Check("Versi Android", Status.FAIL, "API $sdkInt; Winlator 11.2 membutuhkan Android 8.0 (API 26) ke atas")
        }

        val glMajor = glEsVersion ushr 16
        val glMinor = glEsVersion and 0xFFFF
        out += if (glMajor >= 2) {
            Check("OpenGL ES", Status.OK, "$glMajor.$glMinor (minimal 2.0)")
        } else {
            Check("OpenGL ES", Status.FAIL, "$glMajor.$glMinor; Winlator 11.2 mewajibkan OpenGL ES 2.0")
        }

        out += if (vulkanVersion != null && vulkanVersion > 0) {
            val major = vulkanVersion ushr 22
            val minor = (vulkanVersion ushr 12) and 0x3FF
            val patch = vulkanVersion and 0xFFF
            Check("Vulkan", Status.INFO, "$major.$minor.$patch terdeteksi")
        } else {
            Check("Vulkan", Status.INFO, "tidak terdeteksi (sebagian driver grafis Winlator memakai Vulkan)")
        }
        return out
    }
}
