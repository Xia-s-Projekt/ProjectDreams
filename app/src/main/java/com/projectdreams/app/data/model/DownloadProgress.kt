package com.projectdreams.app.data.model

data class DownloadProgress(
    val fraction: Float,
    val status: String? = null,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val currentFileBytes: Long = 0,
    val currentFileTotal: Long = 0,
    val currentFileIndex: Int = 0,
    val fileCount: Int = 0,
    val bytesPerSecond: Float = 0f
) {
    val etaSeconds: Long?
        get() {
            if (bytesPerSecond <= 0f) return null
            val remaining = (totalBytes - downloadedBytes).coerceAtLeast(0)
            if (remaining == 0L) return null
            return (remaining / bytesPerSecond).toLong().coerceAtLeast(1)
        }
}
