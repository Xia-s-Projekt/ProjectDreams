package com.projectdreams.app.data

import android.content.Context
import android.util.Log
import com.projectdreams.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Checks whether the tracked app has an update on Play and either notifies the
 * user or (when auto-update is enabled and a privileged installer is available)
 * performs the update in the background.
 */
class UpdateChecker(
    private val context: Context,
    private val settings: SettingsRepository,
    private val storeRepository: StoreRepository,
    private val pipeline: AppUpdatePipeline
) {

    /**
     * Runs one update pass. Returns a short description of what was done
     * (for logging), or null when nothing needed to happen.
     */
    suspend fun checkAndAct(): String? = withContext(Dispatchers.IO) {
        val notifyEnabled = settings.updateNotifications.value
        val autoUpdate = settings.autoUpdate.value
        if (!notifyEnabled && !autoUpdate) {
            return@withContext null
        }

        val pkg = App.from(context).trackedPackage()
        val installed = InstalledAppInfo.installedVersion(context, pkg) ?: return@withContext null
        val remote = try {
            storeRepository.getApp(pkg)
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed", e)
            return@withContext null
        }

        if (installed.versionCode.toLong() >= remote.versionCode) {
            return@withContext null
        }

        if (autoUpdate) {
            if (pipeline.installManagerReady()) {
                val result = pipeline.run(pkg)
                val message = result.fold(
                    onSuccess = {
                        Log.i(TAG, "Auto-updated to v${remote.versionName}")
                        "hololive Dreams updated to v${remote.versionName}"
                    },
                    onFailure = {
                        Log.e(TAG, "Auto-update failed", it)
                        "Update failed: ${it.message ?: "unknown error"}"
                    }
                )
                if (notifyEnabled) {
                    UpdateNotifier.notify(context, NOTIFICATION_ID_RESULT, "ProjectDreams", message)
                }
                return@withContext message
            } else {
                val message = "Allow root or Shizuku access to ProjectDreams to enable auto-update."
                UpdateNotifier.notify(context, NOTIFICATION_ID_PRIVILEGE, "Update blocked", message)
                return@withContext message
            }
        }

        if (notifyEnabled) {
            UpdateNotifier.notify(
                context,
                NOTIFICATION_ID_UPDATE,
                "Update available",
                "hololive Dreams v${remote.versionName} is now on the Play Store."
            )
        }
        "update available (v${remote.versionName})"
    }

    companion object {
        private const val TAG = "UpdateChecker"
        private const val NOTIFICATION_ID_UPDATE = 1001
        private const val NOTIFICATION_ID_PRIVILEGE = 1002
        private const val NOTIFICATION_ID_RESULT = 1003
    }
}
