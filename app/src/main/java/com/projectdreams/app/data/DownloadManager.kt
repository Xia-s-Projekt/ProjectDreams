package com.projectdreams.app.data

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.aurora.gplayapi.data.models.PlayFile
import com.projectdreams.app.data.model.DownloadProgress
import com.projectdreams.app.data.model.ResumeInfo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.DigestInputStream
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Purchases an app via [StoreRepository] and streams its [PlayFile]s to disk,
 * verifying each against its SHA-256 (falling back to SHA-1) hash.
 *
 * Downloads are resumable: a partially written `.tmp` file is continued with an
 * HTTP Range request instead of being re-downloaded from scratch.
 */
class DownloadManager(
    private val context: Context,
    private val client: PlayHttpClient,
    private val storeRepository: StoreRepository,
    private val stateStore: DownloadStateStore
) {

    /**
     * Downloads [packageName] at [versionCode] for [offerType], emitting overall
     * progress (0f..1f) across all files plus byte counts, speed and ETA.
     */
    fun downloadWithProgress(
        packageName: String,
        versionCode: Long,
        offerType: Int,
        certificateHash: String? = null
    ): Flow<DownloadProgress> = flow {
        // Reuse a previously purchased manifest when present: signed download URLs are
        // short-lived, but re-purchasing on every attempt (tap, retry, relaunch) is what
        // gets us rate-limited by Google Play. Mirroring Aurora Store, we only purchase
        // when nothing is cached, and re-purchase when the URLs expire (HTTP 403/410).
        var files = stateStore.loadFiles(packageName, versionCode)
            ?.takeIf { it.isNotEmpty() }
            ?: storeRepository.purchase(packageName, versionCode, offerType, certificateHash)
        if (files.isEmpty()) {
            throw IllegalStateException("Play returned no downloadable files for this app.")
        }
        stateStore.save(packageName, versionCode, files)

        val dir = downloadDir(packageName, versionCode).apply { mkdirs() }

        val totalBytes = files.sumOf { it.size }
        var doneBytes = 0L
        var speed = 0f
        var lastSampleTotal = 0L
        var lastSampleTime = SystemClock.elapsedRealtime()

        var index = 0
        while (index < files.size) {
            val fileName = sanitizeName(files[index].name)
            val file = File(dir, fileName)
            if (file.exists() && verify(file, files[index])) {
                doneBytes += file.length()
                emit(
                    DownloadProgress(
                        fraction = doneBytes.toFloat() / totalBytes,
                        downloadedBytes = doneBytes,
                        totalBytes = totalBytes,
                        bytesPerSecond = speed
                    )
                )
                index++
                continue
            }

            val tmpFile = File(dir, "$fileName.tmp")
            var attempt = 0
            while (true) {
                try {
                    attempt++
                    val playFile = files[index]
                    val resumeOffset = if (tmpFile.exists()) tmpFile.length() else 0L
                    if (resumeOffset < playFile.size) {
                        downloadSingleFile(playFile, tmpFile, resumeOffset) { fileBytes ->
                            val total = doneBytes + fileBytes
                            val now = SystemClock.elapsedRealtime()
                            val dt = (now - lastSampleTime).coerceAtLeast(1L)
                            if (lastSampleTotal > 0L) {
                                val instant = (total - lastSampleTotal).toFloat() * 1000f / dt
                                speed = if (speed <= 0f) instant else speed * 0.7f + instant * 0.3f
                            }
                            lastSampleTotal = total
                            lastSampleTime = now
                            emit(
                                DownloadProgress(
                                    fraction = total.toFloat() / totalBytes,
                                    status = downloadStatus(index, files.size, fileBytes, playFile.size),
                                    downloadedBytes = total,
                                    totalBytes = totalBytes,
                                    currentFileBytes = fileBytes,
                                    currentFileTotal = playFile.size,
                                    currentFileIndex = index + 1,
                                    fileCount = files.size,
                                    bytesPerSecond = speed
                                )
                            )
                        }
                    }
                    break
                } catch (e: IOException) {
                    if (e is ExpiredUrlException) {
                        // Play download URLs are short-lived; drop the stale manifest and
                        // re-purchase fresh ones, keeping the partial for range-resume.
                        Log.w(TAG, "Download URL for ${files[index].name} expired, re-purchasing")
                        stateStore.clear(packageName, versionCode)
                        val refreshed = storeRepository.purchase(
                            packageName, versionCode, offerType, certificateHash
                        )
                        if (refreshed.isNotEmpty()) {
                            files = refreshed
                            stateStore.save(packageName, versionCode, refreshed)
                            index = index.coerceAtMost(files.size - 1)
                        }
                        continue
                    }
                    tmpFile.delete()
                    if (attempt >= MAX_ATTEMPTS) throw e
                    Log.w(TAG, "Download of ${files[index].name} failed (attempt $attempt), retrying", e)
                    emit(
                        DownloadProgress(
                            fraction = doneBytes.toFloat() / totalBytes,
                            status = "Retrying ${index + 1}/${files.size}…",
                            downloadedBytes = doneBytes,
                            totalBytes = totalBytes,
                            bytesPerSecond = speed
                        )
                    )
                }
            }

            if (!verify(tmpFile, files[index])) {
                tmpFile.delete()
                throw IllegalStateException("Verification failed for ${files[index].name}")
            }
            tmpFile.renameTo(file)
            doneBytes += file.length()
            emit(
                DownloadProgress(
                    fraction = doneBytes.toFloat() / totalBytes,
                    downloadedBytes = doneBytes,
                    totalBytes = totalBytes,
                    bytesPerSecond = speed
                )
            )
            index++
        }
        emit(DownloadProgress(1f, downloadedBytes = totalBytes, totalBytes = totalBytes, bytesPerSecond = speed))
    }.flowOn(Dispatchers.IO)

    /**
     * Streams one file to [target], appending at [resumeOffset] when a partial
     * download already exists (Range request). Falls back to a fresh download if
     * the server ignores the Range header.
     */
    private suspend fun downloadSingleFile(
        playFile: PlayFile,
        target: File,
        resumeOffset: Long,
        onBytes: suspend (Long) -> Unit
    ) {
        val headers = if (resumeOffset > 0L) mapOf("Range" to "bytes=$resumeOffset-") else emptyMap()
        client.call(playFile.url, headers).use { response ->
            when {
                response.code == 206 -> Unit
                response.code == 200 && resumeOffset > 0L -> target.delete() // Range ignored: restart
                response.code == 416 && resumeOffset > 0L -> return // already fully downloaded
                // Signed Play URLs are short-lived; a 403/410 means ours expired while the
                // download sat queued. Clear the manifest so the next attempt re-purchases.
                response.code == 403 || response.code == 410 -> throw ExpiredUrlException(playFile.name)
                !response.isSuccessful -> throw IllegalStateException(
                    "Download failed (HTTP ${response.code}): ${playFile.name}"
                )
            }
            val offset = if (response.code == 206) resumeOffset else 0L
            response.body.byteStream().use { input ->
                FileOutputStream(target, response.code == 206).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var written = offset
                    var lastEmitBytes = offset
                    var lastEmitTime = SystemClock.elapsedRealtime()
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        written += read
                        val now = SystemClock.elapsedRealtime()
                        if (written - lastEmitBytes >= EMIT_INTERVAL_BYTES ||
                            now - lastEmitTime >= EMIT_INTERVAL_MILLIS
                        ) {
                            lastEmitBytes = written
                            lastEmitTime = now
                            onBytes(written)
                        }
                    }
                    output.flush()
                }
            }
        }
    }

    /**
     * What is on disk for a (possibly interrupted) download session, so the UI
     * can offer "Continue" with a rough idea of how far it got.
     */
    suspend fun resumableInfo(packageName: String, versionCode: Long): ResumeInfo =
        withContext(Dispatchers.IO) {
            val dir = downloadDir(packageName, versionCode)
            val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
            val session = stateStore.loadFiles(packageName, versionCode)
            var doneFiles = 0
            var partialFiles = 0
            var bytesOnDisk = 0L
            files.forEach { file ->
                bytesOnDisk += file.length()
                if (file.name.endsWith(TMP_SUFFIX)) partialFiles++ else doneFiles++
            }
            ResumeInfo(
                fileCount = session?.size ?: 0,
                doneFiles = doneFiles,
                partialFiles = partialFiles,
                bytesOnDisk = bytesOnDisk
            )
        }

    private fun downloadDir(packageName: String, versionCode: Long): File =
        File(context.getExternalFilesDir(null), "$packageName/$versionCode")

    /**
     * Verified files previously downloaded for [packageName] at [versionCode], if any.
     */
    suspend fun downloadedFiles(packageName: String, versionCode: Long): List<File> =
        withContext(Dispatchers.IO) {
            downloadDir(packageName, versionCode).listFiles()
                ?.filter { it.isFile && it.extension != "tmp" }
                ?.toList()
                ?: emptyList()
        }

    /**
     * Deletes every downloaded file (and leftover .tmp) for [packageName], freeing
     * storage after a successful install.
     */
    suspend fun clearDownloads(packageName: String) = withContext(Dispatchers.IO) {
        val root = File(context.getExternalFilesDir(null), packageName)
        root.deleteRecursively()
        stateStore.clearAll(packageName)
        Log.i(TAG, "Cleared downloaded files for $packageName")
    }

    private fun downloadStatus(index: Int, count: Int, fileBytes: Long, fileTotal: Long): String =
        "Downloading ${index + 1}/$count — ${Format.size(fileBytes)} of ${Format.size(fileTotal)}"

    private fun sanitizeName(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_")

    private fun verify(file: File, playFile: PlayFile): Boolean {
        val algorithm = if (playFile.sha256.isNotBlank()) "SHA-256" else "SHA-1"
        val expected = if (playFile.sha256.isNotBlank()) playFile.sha256 else playFile.sha1
        if (expected.isBlank()) return false
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            file.inputStream().use { fis ->
                DigestInputStream(fis, digest).use { dis ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (dis.read(buffer) != -1) {
                        // digest updates automatically
                    }
                }
            }
            digest.digest().toHexString() == expected
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify $file", e)
            false
        }
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }

    /** Thrown when a signed download URL returns 403/410 (expired while queued). */
    private class ExpiredUrlException(fileName: String) :
        IOException("Download URL expired for $fileName")

    companion object {
        private const val TAG = "DownloadManager"
        private const val MAX_ATTEMPTS = 3
        private const val EMIT_INTERVAL_BYTES = 512L * 1024
        private const val EMIT_INTERVAL_MILLIS = 1000L
        private const val TMP_SUFFIX = ".tmp"
    }
}
