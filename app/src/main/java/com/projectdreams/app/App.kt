package com.projectdreams.app

import android.app.Application
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.projectdreams.app.data.AppUpdatePipeline
import com.projectdreams.app.data.AuthRepository
import com.projectdreams.app.data.DownloadManager
import com.projectdreams.app.data.DownloadNotifier
import com.projectdreams.app.data.DownloadStateStore
import com.projectdreams.app.data.PlayHttpClient
import com.projectdreams.app.data.Region
import com.projectdreams.app.data.SettingsRepository
import com.projectdreams.app.data.StoreRepository
import com.projectdreams.app.data.UpdateChecker
import com.projectdreams.app.data.UpdateNotifier
import com.projectdreams.app.data.UpdateWorker
import com.projectdreams.app.data.install.InstallManager
import com.projectdreams.app.data.model.DownloadProgress
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient

class App : Application() {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val playHttpClient: PlayHttpClient by lazy {
        PlayHttpClient(okHttpClient)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(this, playHttpClient, settingsRepository)
    }

    val storeRepository: StoreRepository by lazy {
        StoreRepository(authRepository, playHttpClient)
    }

    val downloadStateStore: DownloadStateStore by lazy {
        DownloadStateStore(this)
    }

    val downloadManager: DownloadManager by lazy {
        DownloadManager(this, playHttpClient, storeRepository, downloadStateStore)
    }

    val installManager: InstallManager by lazy {
        InstallManager(this, settingsRepository)
    }

    val downloadNotifier: DownloadNotifier by lazy {
        DownloadNotifier(this)
    }

    val appUpdatePipeline: AppUpdatePipeline by lazy {
        AppUpdatePipeline(this, storeRepository, downloadManager, installManager, downloadNotifier)
    }

    val updateChecker: UpdateChecker by lazy {
        UpdateChecker(this, settingsRepository, storeRepository, appUpdatePipeline)
    }

    private val _downloadActive = MutableStateFlow(false)
    val downloadActive: StateFlow<Boolean> = _downloadActive.asStateFlow()

    private val _lastDownloadProgress = MutableStateFlow(DownloadProgress(0f, null))
    val lastDownloadProgress: StateFlow<DownloadProgress> = _lastDownloadProgress.asStateFlow()

    /** Marks whether a download → install run is currently executing. */
    fun setDownloadActive(active: Boolean) {
        _downloadActive.value = active
    }

    /** Snapshot of the latest progress, so a recreated UI can re-attach. */
    fun noteDownloadProgress(progress: DownloadProgress) {
        _lastDownloadProgress.value = progress
    }

    /** The Play package for the currently selected region. */
    fun trackedPackage(): String = when (settingsRepository.region.value) {
        Region.GLOBAL -> PACKAGE_GLOBAL
        Region.JAPAN -> PACKAGE_JAPAN
    }

    override fun onCreate() {
        super.onCreate()
        UpdateNotifier.createChannel(this)
        downloadNotifier.createChannel()
        scheduleUpdateChecks()
    }

    private fun scheduleUpdateChecks() {
        val intervalHours = settingsRepository.updateIntervalHours.value
        val request = PeriodicWorkRequestBuilder<UpdateWorker>(intervalHours, TimeUnit.HOURS)
            .setInitialDelay(intervalHours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    /** Re-applies the update check schedule (call after changing the interval setting). */
    fun rescheduleUpdateChecks() {
        scheduleUpdateChecks()
    }

    override fun onTerminate() {
        scope.cancel()
        super.onTerminate()
    }

    companion object {
        const val UPDATE_WORK_NAME = "projectdreams.update_check"

        /** Package for each storefront region. */
        const val PACKAGE_GLOBAL = "game.qualiarts.hololive.dreams.com"
        const val PACKAGE_JAPAN = "game.qualiarts.hololive.dreams.jp"

        fun from(context: Context): App = context.applicationContext as App
    }
}
