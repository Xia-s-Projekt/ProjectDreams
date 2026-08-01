package com.projectdreams.app.data

import android.content.Context
import com.projectdreams.app.data.install.InstallManager
import com.projectdreams.app.data.model.DownloadProgress
import java.io.File

/**
 * Shared download → install pipeline, used by both the UI (AppViewModel)
 * and the background update checker (UpdateWorker).
 */
class AppUpdatePipeline(
    private val context: Context,
    private val storeRepository: StoreRepository,
    private val downloadManager: DownloadManager,
    private val installManager: InstallManager,
    private val downloadNotifier: DownloadNotifier
) {

    /** Whether the currently selected privileged installer is usable. */
    suspend fun installManagerReady(): Boolean = installManager.isSelectedAvailable()

    /**
     * Purchases the latest version, downloads every split APK (skipping files that
     * are already on disk and verified), then installs via `pm -i com.android.vending`.
     */
    suspend fun run(
        packageName: String,
        deleteAfterInstall: Boolean = false,
        onProgress: (DownloadProgress) -> Unit = {},
        onInstalling: () -> Unit = {}
    ): Result<String> = try {
        val latest = storeRepository.getApp(packageName)
        val certHash = InstalledAppInfo.certificateHash(context, packageName)

        try {
            downloadManager.downloadWithProgress(
                packageName,
                latest.versionCode,
                latest.offerType,
                certHash
            ).collect { progress ->
                onProgress(progress)
                downloadNotifier.showProgress(progress)
            }
        } catch (e: Exception) {
            downloadNotifier.dismiss()
            throw e
        }

        val files: List<File> = downloadManager.downloadedFiles(packageName, latest.versionCode)
        check(files.isNotEmpty()) { "Downloaded files are missing on disk" }

        if (!installManager.isSelectedAvailable()) {
            throw IllegalStateException(
                "Selected installer (${installManager.activeMode.value}) is not available. " +
                    "Grant root or Shizuku permission first."
            )
        }

        onInstalling()
        val result = installManager.install(files, files.sumOf { it.length() })
        downloadNotifier.dismiss()
        val installed = result.getOrThrow()
        if (deleteAfterInstall) {
            downloadManager.clearDownloads(packageName)
        }
        downloadNotifier.showDone("Downloaded and installed v${latest.versionName}")
        Result.success(installed)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Reinstalls an already-downloaded version from disk with the Play Store as
     * the installer (`-i com.android.vending`), without downloading anything —
     * used to fix the install-source attribution when the package was installed
     * by something else. The existing install is replaced in place (signature
     * must match; data is kept).
     */
    suspend fun installFromCache(
        packageName: String,
        versionCode: Long,
        onInstalling: () -> Unit = {}
    ): Result<String> = try {
        val files: List<File> = downloadManager.downloadedFiles(packageName, versionCode)
        check(files.isNotEmpty()) {
            "No downloaded files cached for $packageName v$versionCode — install normally first."
        }

        if (!installManager.isSelectedAvailable()) {
            throw IllegalStateException(
                "Selected installer (${installManager.activeMode.value}) is not available. " +
                    "Grant root or Shizuku permission first."
            )
        }

        onInstalling()
        val result = installManager.install(files, files.sumOf { it.length() })
        downloadNotifier.dismiss()
        val installed = result.getOrThrow()
        downloadNotifier.showDone("Reinstalled with Play Store attribution")
        Result.success(installed)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
