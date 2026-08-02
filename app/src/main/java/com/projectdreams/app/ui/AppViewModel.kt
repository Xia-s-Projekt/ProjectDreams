package com.projectdreams.app.ui

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.App
import com.projectdreams.app.App as ProjectDreamsApp
import com.projectdreams.app.data.Format
import com.projectdreams.app.data.InstalledAppInfo
import com.projectdreams.app.data.Region
import com.projectdreams.app.data.Game
import com.projectdreams.app.data.install.InstallManager
import com.projectdreams.app.data.model.DownloadProgress
import com.projectdreams.app.data.model.ResumeInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppUiState {
    data object Loading : AppUiState
    data class Error(val message: String) : AppUiState
    data class Ready(
        val app: App,
        val installedVersionCode: Long?,
        val installedVersionName: String?,
        val installSource: String? = null
    ) : AppUiState {
        val isInstalled: Boolean get() = installedVersionCode != null
        val isUpToDate: Boolean
            get() = installedVersionCode != null && installedVersionCode >= app.versionCode
        val installedByPlayStore: Boolean
            get() = installSource == "com.android.vending"
    }
}

sealed interface InstallUiState {
    data object Idle : InstallUiState
    data object Preparing : InstallUiState
    data class Downloading(
        val progress: Float,
        val status: String?,
        val detail: DownloadProgress = DownloadProgress(0f, null)
    ) : InstallUiState
    data object Installing : InstallUiState
    data class Done(val message: String, val justInstalled: Boolean = false) : InstallUiState
    data class Failed(
        val message: String,
        val logs: String,
        val isNetworkError: Boolean = false
    ) : InstallUiState
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app: ProjectDreamsApp = application as ProjectDreamsApp

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _installState = MutableStateFlow<InstallUiState>(InstallUiState.Idle)
    val installState: StateFlow<InstallUiState> = _installState.asStateFlow()

    private val _resumeInfo = MutableStateFlow<ResumeInfo?>(null)
    val resumeInfo: StateFlow<ResumeInfo?> = _resumeInfo.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _gameDetails = MutableStateFlow<Map<com.projectdreams.app.data.Game, com.aurora.gplayapi.data.models.App>>(emptyMap())
    val gameDetails: StateFlow<Map<com.projectdreams.app.data.Game, com.aurora.gplayapi.data.models.App>> = _gameDetails.asStateFlow()

    fun loadAllGames() {
        viewModelScope.launch {
            val map = mutableMapOf<com.projectdreams.app.data.Game, com.aurora.gplayapi.data.models.App>()
            allGames.value.forEach { game ->
                try {
                    map[game] = app.storeRepository.getApp(game.glPackage)
                } catch (e: Exception) {}
            }
            _gameDetails.value = map
        }
    }

    private var downloadJob: Job? = null

    /** Set when the notification asks to resume an interrupted download. */
    private var resumeRequested = false

    val onboarded = app.settingsRepository.onboarded
    val updateNotifications = app.settingsRepository.updateNotifications
    val autoUpdate = app.settingsRepository.autoUpdate
    val updateIntervalHours = app.settingsRepository.updateIntervalHours
    val region = app.settingsRepository.region
    val allGames = app.settingsRepository.gamesList
    val game = app.settingsRepository.game

    fun setGame(game: com.projectdreams.app.data.Game) {
        if (_isRefreshing.value) {
            android.widget.Toast.makeText(app, "Please wait while the app is loading", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        app.settingsRepository.setGame(game)
        loadApp()
    }
    val confirmInstallMethod = app.settingsRepository.confirmInstallMethod
    val deleteAfterInstall = app.settingsRepository.deleteAfterInstall
    val rootAvailable = app.installManager.rootAvailable
    val shizukuAvailable = app.installManager.shizukuAvailable
    val activeMode = app.installManager.activeMode

    fun trackedPackage(): String = app.trackedPackage()

    /** Re-checks root/Shizuku availability (e.g. after granting Shizuku permission). */
    fun refreshInstallAvailability(forceRootRecheck: Boolean = false) {
        viewModelScope.launch {
            app.installManager.refreshAvailability(forceRootRecheck)
        }
    }


    suspend fun searchApps(query: String, region: com.projectdreams.app.data.Region = app.settingsRepository.region.value): List<App> {
        return app.storeRepository.searchApps(query, region)
    }

    suspend fun getApp(packageName: String): App {
        return app.storeRepository.getApp(packageName)
    }
    
    fun addGameConfig(game: com.projectdreams.app.data.Game) {
        app.settingsRepository.addGame(game)
    }

    init {
        if (app.downloadActive.value) {
            val last = app.lastDownloadProgress.value
            _installState.value = InstallUiState.Downloading(last.fraction, last.status, last)
        }
        viewModelScope.launch {
            app.installManager.refreshAvailability()
            loadAllGames()
            loadApp()
        }
    }

    fun loadApp() {
        viewModelScope.launch {
            if (_uiState.value !is AppUiState.Ready) {
                _uiState.value = AppUiState.Loading
            }
            _isRefreshing.value = true
            runCatching {
                val gplayApp = app.storeRepository.getApp(trackedPackage())
                val installed = InstalledAppInfo.installedVersion(getApplication(), trackedPackage())
                AppUiState.Ready(
                    gplayApp,
                    installed?.versionCode?.toLong(),
                    installed?.versionName,
                    InstalledAppInfo.installerPackage(getApplication(), trackedPackage())
                )
            }.onSuccess {
                _uiState.value = it
                viewModelScope.launch {
                    _resumeInfo.value = computeResumeInfo()
                    tryAutoResume()
                }
            }
                .onFailure {
                    Log.e(TAG, "Failed to load app", it)
                    if (_uiState.value is AppUiState.Ready) {
                        _uiState.value = AppUiState.Error(it.message ?: "Failed to load app details")
                    } else {
                        _uiState.value = AppUiState.Error(it.message ?: "Failed to load app details")
                    }
                }
            _isRefreshing.value = false
        }
    }

    fun completeSetup(mode: InstallManager.Mode) {
        app.installManager.selectMode(mode)
        app.settingsRepository.setOnboarded(true)
        viewModelScope.launch {
            app.installManager.refreshAvailability()
            loadAllGames()
            loadApp()
        }
    }

    fun selectInstallMode(mode: InstallManager.Mode) {
        app.installManager.selectMode(mode)
    }

    fun refreshPrivilegeStatus(forceRootRecheck: Boolean = false) {
        viewModelScope.launch {
            app.installManager.refreshAvailability(forceRootRecheck)
        }
    }

    private val _fixSourceBusy = MutableStateFlow(false)
    val fixSourceBusy: StateFlow<Boolean> = _fixSourceBusy.asStateFlow()

    private val _fixSourceError = MutableStateFlow<String?>(null)
    val fixSourceError: StateFlow<String?> = _fixSourceError.asStateFlow()

    /**
     * Fixes the install-source attribution when the tracked package is installed
     * but NOT by the Play Store, without downloading anything:
     *  1. `pm set-installer` (instant, but blocked on recent Android builds),
     *  2. reinstall from ProjectDreams' own cached downloads,
     *  3. reinstall from the package's installed APK files themselves (`pm path`)
     *     — works even when the app was installed by another store.
     */
    fun fixInstallSource() {
        if (_fixSourceBusy.value) return
        _fixSourceBusy.value = true
        _fixSourceError.value = null
        viewModelScope.launch {
            try {
                val state = _uiState.value as? AppUiState.Ready
                val pkg = trackedPackage()
                val vending = "com.android.vending"

                if (app.installManager.setInstaller(pkg, vending).isSuccess) {
                    toast("Install source fixed")
                    return@launch
                }
                Log.w(TAG, "pm set-installer not permitted, falling back to reinstall")

                val versionCode = state?.app?.versionCode
                    ?: InstalledAppInfo.installedVersion(getApplication(), pkg)?.versionCode?.toLong()
                    ?: throw IllegalStateException("Cannot determine installed version")

                val cached = app.downloadManager.downloadedFiles(pkg, versionCode)
                val result = if (cached.isNotEmpty()) {
                    app.appUpdatePipeline.installFromCache(pkg, versionCode)
                } else {
                    app.installManager.reinstallFromInstalled(pkg, vending)
                }
                result.onSuccess { toast("Fixed!") }
                    .onFailure {
                        Log.e(TAG, "Source fix failed", it)
                        _fixSourceError.value = it.message
                    }
            } finally {
                _fixSourceBusy.value = false
                loadApp()
            }
        }
    }

    fun requestShizukuPermission() {
        app.installManager.requestShizukuPermission()
    }

    fun setUpdateNotifications(enabled: Boolean) {
        app.settingsRepository.setUpdateNotifications(enabled)
    }

    fun setAutoUpdate(enabled: Boolean) {
        app.settingsRepository.setAutoUpdate(enabled)
    }

    fun openApp() {
        val launchIntent = getApplication<Application>().packageManager
            .getLaunchIntentForPackage(trackedPackage()) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            getApplication<Application>().startActivity(launchIntent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "Cannot launch ${trackedPackage()}", e)
        }
    }

    /** Opens the app's page in the Play Store app, falling back to the web store. */
    fun openPlayStore() {
        val context = getApplication<Application>()
        val packageName = trackedPackage()
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(market)
            return
        } catch (_: Exception) {
        }
        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(web)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No Play Store or browser available", e)
        }
    }

    fun uninstall() {
        val state = _installState.value
        if (state is InstallUiState.Downloading ||
            state is InstallUiState.Installing ||
            state is InstallUiState.Preparing
        ) return
        downloadJob = viewModelScope.launch {
            _installState.value = InstallUiState.Preparing
            if (!app.installManager.isSelectedAvailable()) {
                _installState.value = InstallUiState.Failed(
                    "Selected installer (${app.installManager.activeMode.value}) is not " +
                        "available. Grant root or Shizuku permission first.",
                    "installer: ${app.installManager.activeMode.value} not available"
                )
                return@launch
            }
            app.installManager.uninstall(trackedPackage())
                .onSuccess {
                    _installState.value = InstallUiState.Idle
                    toast("Uninstalled")
                    loadApp()
                }
                .onFailure {
                    Log.e(TAG, "Uninstall failed", it)
                    _installState.value = InstallUiState.Failed(
                        it.message ?: "Uninstall failed",
                        it.stackTraceToString()
                    )
                }
        }
    }

    /** Runs the full download → install pipeline (in the app scope so it keeps
     *  running while the UI is backgrounded). */
    fun installOrUpdate() {
        val state = _uiState.value as? AppUiState.Ready ?: return
        if (_installState.value is InstallUiState.Downloading ||
            _installState.value is InstallUiState.Installing ||
            _installState.value is InstallUiState.Preparing
        ) return
        if (app.downloadActive.value) return

        val deleteAfter = app.settingsRepository.deleteAfterInstall.value
        app.setDownloadActive(true)
        downloadJob = app.scope.launch {
            try {
                _installState.value = InstallUiState.Preparing
                app.appUpdatePipeline.run(
                    packageName = trackedPackage(),
                    deleteAfterInstall = deleteAfter,
                    onProgress = { progress: DownloadProgress ->
                        app.noteDownloadProgress(progress)
                        _installState.value = InstallUiState.Downloading(
                            progress.fraction,
                            progress.status,
                            progress
                        )
                    },
                    onInstalling = {
                        _installState.value = InstallUiState.Installing
                    }
                ).onSuccess {
                    _installState.value = InstallUiState.Done(it, justInstalled = true)
                    loadApp()
                }.onFailure { e ->
                    if (e is kotlinx.coroutines.CancellationException) return@launch
                    Log.e(TAG, "Download/install pipeline failed", e)
                    _installState.value = InstallUiState.Failed(
                        message = if (isNetworkError(e)) {
                            "No Internet Connection"
                        } else {
                            e.message ?: "Download failed"
                        },
                        logs = e.stackTraceToString(),
                        isNetworkError = isNetworkError(e)
                    )
                    refreshResumeInfo()
                }
            } finally {
                app.setDownloadActive(false)
            }
        }
    }

    private fun isNetworkError(e: Throwable): Boolean =
        e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.net.SocketTimeoutException ||
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
            e.message?.contains("Failed to connect", ignoreCase = true) == true ||
            e.message?.contains("connect timed out", ignoreCase = true) == true

    fun cancelDownload() {
        val hadJob = downloadJob?.isActive == true
        downloadJob?.cancel()
        downloadJob = null
        _installState.value = InstallUiState.Idle
        refreshResumeInfo()
        if (hadJob) toast("Download cancelled")
    }

    /** Closes the failure dialog, returning to the idle state. */
    fun dismissFailure() {
        _installState.value = InstallUiState.Idle
        refreshResumeInfo()
    }

    /** Called when the download notification is tapped: continue a paused download. */
    fun requestResume() {
        resumeRequested = true
        tryAutoResume()
    }

    private fun tryAutoResume() {
        if (!resumeRequested) return
        // A run may have just finished/failed — never start a second one while
        // the UI is still in a non-idle state (Done, Failed, Downloading…).
        if (_installState.value !is InstallUiState.Idle) {
            resumeRequested = false
            return
        }
        val state = _uiState.value as? AppUiState.Ready ?: return
        if (app.downloadActive.value) {
            resumeRequested = false
            return
        }
        if (state.isUpToDate) {
            resumeRequested = false
            return
        }
        val info = _resumeInfo.value
        if (info == null || !info.hasPartial) {
            resumeRequested = false
            return
        }
        resumeRequested = false
        installOrUpdate()
    }

    private suspend fun computeResumeInfo(): ResumeInfo {
        val state = _uiState.value as? AppUiState.Ready ?: return ResumeInfo(0, 0, 0, 0)
        return app.downloadManager.resumableInfo(trackedPackage(), state.app.versionCode)
    }

    private fun refreshResumeInfo() {
        viewModelScope.launch {
            _resumeInfo.value = computeResumeInfo()
        }
    }

    fun setRegion(value: Region) {
        if (_isRefreshing.value) {
            android.widget.Toast.makeText(app, "Please wait while the app is loading", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (value == app.settingsRepository.region.value) return
        app.settingsRepository.setRegion(value)
        _installState.value = InstallUiState.Idle
        loadApp()
    }

    fun setConfirmInstallMethod(value: Boolean) {
        app.settingsRepository.setConfirmInstallMethod(value)
    }

    fun setDeleteAfterInstall(value: Boolean) {
        app.settingsRepository.setDeleteAfterInstall(value)
    }

    fun setUpdateIntervalHours(hours: Long) {
        app.settingsRepository.setUpdateIntervalHours(hours)
        app.rescheduleUpdateChecks()
    }

    /** Runs one update check pass immediately (for the "Check now" button). */
    fun checkNow() {
        viewModelScope.launch {
            val result = runCatching { app.updateChecker.checkAndAct() }
            if (result.isSuccess && result.getOrNull() == null) {
                toast("No update found")
            } else if (result.isFailure) {
                toast("Check failed: ${result.exceptionOrNull()?.message ?: "unknown error"}")
            } else {
                toast(result.getOrNull() ?: "Check done")
            }
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            app.downloadManager.clearDownloads(trackedPackage())
            refreshResumeInfo()
            toast("Downloads cleared")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    /** Whether the app can currently post notifications (Android 13+). */
    fun notificationsGranted(): Boolean = ContextCompat.checkSelfPermission(
        getApplication(),
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    fun formatSize(bytes: Long): String = Format.size(bytes)

    fun formatSpeed(bytesPerSecond: Float): String = Format.speed(bytesPerSecond)

    fun formatEta(seconds: Long): String = Format.eta(seconds)

    fun formatDate(updatedOn: String): String {
        val date = parseDate(updatedOn) ?: return updatedOn
        return try {
            SimpleDateFormat("MMM d, yyyy", Locale.US).format(date)
        } catch (_: Exception) {
            updatedOn
        }
    }

    /** Accepts epoch millis, epoch seconds, and common ISO date strings. */
    private fun parseDate(raw: String): Date? {
        raw.toLongOrNull()?.let { millis ->
            return if (millis > 100_000_000_000L) {
                Date(millis)
            } else {
                Date(millis * 1000)
            }
        }
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "MMM d, yyyy",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (format in formats) {
            try {
                SimpleDateFormat(format, Locale.US).parse(raw)?.let { return it }
            } catch (_: Exception) {
            }
        }
        return null
    }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
