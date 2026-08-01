package com.projectdreams.app.data.install

import java.io.File

/**
 * Installs an already-downloaded set of APK files with the `pm` tool,
 * reporting the Play Store as the installer (`-i com.android.vending`).
 */
interface Installer {
    val name: String

    /** Whether the underlying privilege (root shell / Shizuku server) is available. */
    suspend fun isAvailable(): Boolean

    /** Installs [files] (total size [totalBytes]) and returns the `pm` output. */
    suspend fun install(files: List<File>, totalBytes: Long): Result<String>

    /** Uninstalls [packageName] and returns the `pm` output. */
    suspend fun uninstall(packageName: String): Result<String>

    /** Re-attributes [packageName] to [installerPackage] via `pm set-installer`.
     *  Fails on recent Android builds (shell cert check / root uid rejection). */
    suspend fun setInstaller(packageName: String, installerPackage: String): Result<String>

    /**
     * Reinstalls an already-installed package in place using its own installed
     * APK files (`pm path`) — no download needed. Used to fix the install-source
     * attribution when the package was installed by another store.
     */
    suspend fun reinstallFromInstalled(packageName: String, installerPackage: String): Result<String>
}
