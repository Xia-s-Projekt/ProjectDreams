package com.projectdreams.app.data

import android.content.Context
import com.projectdreams.app.data.install.InstallManager.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Play storefront region. GL = global build, JP = Japanese build. */

enum class Game(val glPackage: String, val jpPackage: String, val fallbackName: String) {
    HOLOLIVE_DREAMS("game.qualiarts.hololive.dreams.com", "game.qualiarts.hololive.dreams.jp", "Hololive Dreams"),
    PROJECT_SEKAI("com.sega.ColorfulStage.en", "com.sega.pjsekai", "Project Sekai")
}

/** Play storefront region. GL = global build, JP = Japanese build. */
enum class Region(val label: String) {
    GLOBAL("Global"),
    JAPAN("Japan")
}

/**
 * Lightweight SharedPreferences-backed settings holder.
 */
class SettingsRepository(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _onboarded = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDED, false))
    val onboarded: StateFlow<Boolean> = _onboarded.asStateFlow()

    private val _installMode = MutableStateFlow(
        runCatching { Mode.valueOf(prefs.getString(KEY_INSTALL_MODE, null) ?: "") }
            .getOrDefault(Mode.ROOT)
    )
    val installMode: StateFlow<Mode> = _installMode.asStateFlow()

    private val _updateNotifications = MutableStateFlow(
        prefs.getBoolean(KEY_UPDATE_NOTIFICATIONS, false)
    )
    val updateNotifications: StateFlow<Boolean> = _updateNotifications.asStateFlow()

    private val _autoUpdate = MutableStateFlow(prefs.getBoolean(KEY_AUTO_UPDATE, false))
    val autoUpdate: StateFlow<Boolean> = _autoUpdate.asStateFlow()

    
    private val _game = MutableStateFlow(
        runCatching { Game.valueOf(prefs.getString(KEY_GAME, null) ?: "") }
            .getOrDefault(Game.HOLOLIVE_DREAMS)
    )
    val game: StateFlow<Game> = _game.asStateFlow()

    private val _region = MutableStateFlow(
        runCatching { Region.valueOf(prefs.getString(KEY_REGION, null) ?: "") }
            .getOrDefault(Region.GLOBAL)
    )
    val region: StateFlow<Region> = _region.asStateFlow()

    private val _confirmInstallMethod = MutableStateFlow(
        prefs.getBoolean(KEY_CONFIRM_INSTALL_METHOD, true)
    )
    val confirmInstallMethod: StateFlow<Boolean> = _confirmInstallMethod.asStateFlow()

    private val _deleteAfterInstall = MutableStateFlow(
        prefs.getBoolean(KEY_DELETE_AFTER_INSTALL, false)
    )
    val deleteAfterInstall: StateFlow<Boolean> = _deleteAfterInstall.asStateFlow()

    private val _updateIntervalHours = MutableStateFlow(loadUpdateIntervalHours())
    val updateIntervalHours: StateFlow<Long> = _updateIntervalHours.asStateFlow()

    fun setOnboarded(value: Boolean) {
        _onboarded.value = value
        prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()
    }

    fun setInstallMode(mode: Mode) {
        _installMode.value = mode
        prefs.edit().putString(KEY_INSTALL_MODE, mode.name).apply()
    }

    fun setUpdateNotifications(value: Boolean) {
        _updateNotifications.value = value
        prefs.edit().putBoolean(KEY_UPDATE_NOTIFICATIONS, value).apply()
    }

    fun setAutoUpdate(value: Boolean) {
        _autoUpdate.value = value
        prefs.edit().putBoolean(KEY_AUTO_UPDATE, value).apply()
    }

    
    fun setGame(value: Game) {
        _game.value = value
        prefs.edit().putString(KEY_GAME, value.name).apply()
    }

    fun setRegion(value: Region) {
        _region.value = value
        prefs.edit().putString(KEY_REGION, value.name).apply()
    }

    fun setConfirmInstallMethod(value: Boolean) {
        _confirmInstallMethod.value = value
        prefs.edit().putBoolean(KEY_CONFIRM_INSTALL_METHOD, value).apply()
    }

    fun setDeleteAfterInstall(value: Boolean) {
        _deleteAfterInstall.value = value
        prefs.edit().putBoolean(KEY_DELETE_AFTER_INSTALL, value).apply()
    }

    fun setUpdateIntervalHours(hours: Long) {
        val clamped = hours.coerceIn(MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS)
        _updateIntervalHours.value = clamped
        prefs.edit().putLong(KEY_UPDATE_INTERVAL_HOURS, clamped).apply()
    }

    private fun loadUpdateIntervalHours(): Long {
        val custom = prefs.getLong(KEY_UPDATE_INTERVAL_HOURS, -1L)
        if (custom > 0) return custom.coerceIn(MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS)
        // Migrate from the legacy named presets.
        val hours = when (prefs.getString(KEY_UPDATE_INTERVAL_LEGACY, null)) {
            "HOURLY" -> 1L
            "WEEKLY" -> 168L
            "MONTHLY" -> 720L
            else -> 24L
        }
        prefs.edit().remove(KEY_UPDATE_INTERVAL_LEGACY).apply()
        return hours
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_INSTALL_MODE = "install_mode"
        private const val KEY_UPDATE_NOTIFICATIONS = "update_notifications"
        private const val KEY_AUTO_UPDATE = "auto_update"
        private const val KEY_GAME = "game"
        const val KEY_REGION = "region"
        private const val KEY_CONFIRM_INSTALL_METHOD = "confirm_install_method"
        private const val KEY_DELETE_AFTER_INSTALL = "delete_after_install"
        private const val KEY_UPDATE_INTERVAL_HOURS = "update_interval_hours"
        private const val KEY_UPDATE_INTERVAL_LEGACY = "update_interval"

        /** Background check runs no more often than hourly and no less often than monthly. */
        const val MIN_INTERVAL_HOURS = 1L
        const val MAX_INTERVAL_HOURS = 720L
    }
}
