package com.projectdreams.app.data

import android.util.Log
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps the GPlayApi helpers that talk directly to Google Play's backend:
 * - [AppDetailsHelper] fetches store listings (name, icon, screenshots, changelog…)
 * - [PurchaseHelper] buys/"acquires" the app and returns signed download URLs.
 *
 * When a game is region-locked to Japan and the anonymous session cannot
 * acquire it (Google checks the request IP), the repository transparently
 * retries through a user-configured SOCKS5 proxy so the acquire call
 * appears to originate from Japan.
 */
class StoreRepository(
    private val authRepository: AuthRepository,
    private val client: PlayHttpClient,
    private val settingsRepository: SettingsRepository
) {

    suspend fun getApp(packageName: String): App = withContext(Dispatchers.IO) {
        val authData = authRepository.authData()
        AppDetailsHelper(authData)
            .using(client)
            .getAppByPackageName(packageName)
    }

    suspend fun searchApps(query: String, region: com.projectdreams.app.data.Region = com.projectdreams.app.data.Region.GLOBAL): List<App> = withContext(Dispatchers.IO) {
        val locale = if (region == com.projectdreams.app.data.Region.JAPAN) java.util.Locale.JAPAN else java.util.Locale.getDefault()
        var results = emptyList<App>()
        try {
            val bundle = com.aurora.gplayapi.helpers.web.WebSearchHelper()
                .using(client)
                .with(locale)
                .searchResults(query, "")
            results = bundle.streamClusters.values.flatMap { it.clusterAppList }.distinctBy { it.packageName }
            
            if (region == com.projectdreams.app.data.Region.JAPAN) {
                 results = results.sortedByDescending { it.packageName.contains("jp", ignoreCase = true) || it.displayName?.contains(query, ignoreCase = true) == true }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (results.isEmpty()) {
            val authData = authRepository.authData()
            val bundle = com.aurora.gplayapi.helpers.SearchHelper(authData)
                .using(client)
                .searchResults(query, "")
            results = bundle.streamClusters.values.flatMap { it.clusterAppList }.distinctBy { it.packageName }
        }
        results
    }

    suspend fun purchase(
        packageName: String,
        versionCode: Long,
        offerType: Int,
        certificateHash: String? = null
    ): List<PlayFile> = withContext(Dispatchers.IO) {
        val authData = authRepository.authData()
        val helper = PurchaseHelper(authData).using(client) as PurchaseHelper
        try {
            helper.purchase(packageName, versionCode, offerType, certificateHash)
        } catch (e: Exception) {
            val isRegionBlock =
                e.javaClass.simpleName.contains("AppNotPurchased") ||
                e.message?.contains("not purchased") == true

            if (!isRegionBlock) throw e

            Log.w(TAG, "Purchase blocked for $packageName, attempting proxy-routed acquire...")

            // ── Attempt 1: acquire with current (direct) session ──
            if (helper.acquire(packageName, versionCode, offerType)) {
                Log.i(TAG, "Direct acquire succeeded")
                return@withContext helper.purchase(packageName, versionCode, offerType, certificateHash)
            }

            // ── Attempt 2: acquire through Japan proxy ──
            if (!settingsRepository.hasProxy()) {
                throw IllegalStateException(
                    "This game requires a Japan IP to acquire. " +
                    "Configure a Japan SOCKS5 proxy in Settings → Japan Proxy to download region-locked games.",
                    e
                )
            }

            val proxyHost = settingsRepository.proxyHost.value
            val proxyPort = settingsRepository.proxyPort.value
            Log.i(TAG, "Routing acquire through proxy $proxyHost:$proxyPort")

            val proxiedClient = ProxiedPlayHttpClient(
                directClient = client.rawClient,
                proxyHost = proxyHost,
                proxyPort = proxyPort
            )

            // Re-roll fresh JP sessions and acquire through the proxy
            var lastError: String? = null
            repeat(3) { attempt ->
                try {
                    val jpAuthData = authRepository.refreshAuth(Region.JAPAN)
                    val jpHelper = PurchaseHelper(jpAuthData).using(proxiedClient) as PurchaseHelper

                    if (jpHelper.acquire(packageName, versionCode, offerType)) {
                        Log.i(TAG, "Proxy acquire succeeded on attempt ${attempt + 1}")
                        return@withContext jpHelper.purchase(packageName, versionCode, offerType, certificateHash)
                    } else {
                        lastError = "acquire returned false on attempt ${attempt + 1}"
                        Log.w(TAG, lastError!!)
                    }
                } catch (e2: Exception) {
                    lastError = e2.message
                    Log.w(TAG, "Proxy acquire attempt ${attempt + 1} failed: ${e2.message}")
                }
            }

            throw IllegalStateException(
                "Region-locked acquire failed after 3 proxy attempts ($lastError). " +
                "Verify your Japan proxy is working and reachable.",
                e
            )
        }
    }

    companion object {
        private const val TAG = "StoreRepository"
    }
}
