package com.gamehouse.doctor

import java.util.Locale

fun formatSize(bytes: Long): String = when {
    bytes >= 1L shl 30 -> String.format(Locale.getDefault(), "%.2f GB", bytes / 1073741824.0)
    bytes >= 1L shl 20 -> String.format(Locale.getDefault(), "%.1f MB", bytes / 1048576.0)
    bytes >= 1L shl 10 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
}
