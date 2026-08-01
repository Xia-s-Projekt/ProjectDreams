package com.projectdreams.app.data

import java.util.Locale

/** Shared human-readable formatting helpers. */
object Format {

    fun size(bytes: Long): String {
        val kb = 1024L
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            bytes >= gb -> String.format(Locale.US, "%.1f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / mb)
            bytes >= kb -> String.format(Locale.US, "%.0f KB", bytes.toDouble() / kb)
            else -> "$bytes B"
        }
    }

    fun speed(bytesPerSecond: Float): String {
        val kb = 1024f
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            bytesPerSecond >= gb -> String.format(Locale.US, "%.1f GB/s", bytesPerSecond / gb)
            bytesPerSecond >= mb -> String.format(Locale.US, "%.1f MB/s", bytesPerSecond / mb)
            bytesPerSecond >= kb -> String.format(Locale.US, "%.0f KB/s", bytesPerSecond / kb)
            else -> String.format(Locale.US, "%.0f B/s", bytesPerSecond)
        }
    }

    fun eta(seconds: Long): String {
        if (seconds < 60) return "~$seconds s"
        val minutes = seconds / 60
        if (minutes < 60) return "~$minutes min"
        val hours = minutes / 60
        val remMinutes = minutes % 60
        if (hours < 24) {
            return if (remMinutes > 0) "~$hours h $remMinutes min" else "~$hours h"
        }
        val days = hours / 24
        val remHours = hours % 24
        return if (remHours > 0) "~$days d $remHours h" else "~$days d"
    }
}
