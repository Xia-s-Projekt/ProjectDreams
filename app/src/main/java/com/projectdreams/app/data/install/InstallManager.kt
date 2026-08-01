package com.projectdreams.app.data.install

import android.content.Context
import android.content.pm.PackageManager
import com.projectdreams.app.data.SettingsRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/**
 * Picks and runs the best available privileged installer.
 */
class InstallManager(
    private val context: Context,
    private val settings: SettingsRepository
) {

    enum class Mode { ROOT, SHIZUKU }

    private val _rootAvailable = MutableStateFlow(false)
    val rootAvailable: StateFlow<Boolean> = _rootAvailable.asStateFlow()

    private val _shizukuAvailable = MutableStateFlow(false)
    val shizukuAvailable: StateFlow<Boolean> = _shizukuAvailable.asStateFlow()

    private val _activeMode = MutableStateFlow(settings.installMode.value)
    val activeMode: StateFlow<Mode> = _activeMode.asStateFlow()

    private val rootInstaller = RootInstaller()
    private val shizukuInstaller = ShizukuInstaller()

    /** Refreshes availability of both privileged backends. When [forceRootRecheck]
     *  is set, the cached root shell is dropped first so a grant made in the root
     *  manager while the app was running is picked up immediately. */
    suspend fun refreshAvailability(forceRootRecheck: Boolean = false) = withContext(Dispatchers.IO) {
        _rootAvailable.value = if (forceRootRecheck) rootInstaller.refresh() else rootInstaller.isAvailable()
        _shizukuAvailable.value = shizukuInstaller.isAvailable()
    }

    /** Selects the mode used for install/uninstall, persisting it across restarts. */
    fun selectMode(mode: Mode) {
        _activeMode.value = mode
        settings.setInstallMode(mode)
    }

    suspend fun isSelectedAvailable(): Boolean = withContext(Dispatchers.IO) {
        when (_activeMode.value) {
            Mode.ROOT -> rootInstaller.isAvailable()
            Mode.SHIZUKU -> shizukuInstaller.isAvailable()
        }
    }

    suspend fun install(files: List<File>, totalBytes: Long): Result<String> =
        withContext(Dispatchers.IO) {
            val installer = when (_activeMode.value) {
                Mode.ROOT -> rootInstaller
                Mode.SHIZUKU -> shizukuInstaller
            }
            installer.install(files, totalBytes)
        }

    suspend fun uninstall(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        val installer = when (_activeMode.value) {
            Mode.ROOT -> rootInstaller
            Mode.SHIZUKU -> shizukuInstaller
        }
        installer.uninstall(packageName)
    }

    /** Re-attributes [packageName] to [installerPackage] via `pm set-installer`. */
    suspend fun setInstaller(packageName: String, installerPackage: String): Result<String> =
        withContext(Dispatchers.IO) {
            val installer = when (_activeMode.value) {
                Mode.ROOT -> rootInstaller
                Mode.SHIZUKU -> shizukuInstaller
            }
            installer.setInstaller(packageName, installerPackage)
        }

    /** Reinstalls [packageName] from its own installed APKs with [installerPackage] attribution. */
    suspend fun reinstallFromInstalled(packageName: String, installerPackage: String): Result<String> =
        withContext(Dispatchers.IO) {
            val installer = when (_activeMode.value) {
                Mode.ROOT -> rootInstaller
                Mode.SHIZUKU -> shizukuInstaller
            }
            installer.reinstallFromInstalled(packageName, installerPackage)
        }

    /** Requests Shizuku runtime permission (must be called on the UI thread). */
    fun requestShizukuPermission() {
        if (Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED
        ) {
            Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
        }
    }

    companion object {
        const val REQUEST_CODE_SHIZUKU = 1001
    }
}
