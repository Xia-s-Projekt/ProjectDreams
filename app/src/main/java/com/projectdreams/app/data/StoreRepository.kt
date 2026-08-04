package com.projectdreams.app.data

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
 */
class StoreRepository(
    private val authRepository: AuthRepository,
    private val client: PlayHttpClient
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
            if (e.javaClass.simpleName.contains("AppNotPurchased") || e.message?.contains("not purchased") == true) {
                // First try acquiring with current session
                if (helper.acquire(packageName, versionCode, offerType)) {
                    return@withContext helper.purchase(packageName, versionCode, offerType, certificateHash)
                }

                // Try to acquire using fresh Japan sessions up to 3 times
                var acquired = false
                var jpHelper = helper
                var lastAcquireError: String? = null
                var lastPurchaseError: Exception? = null

                repeat(3) { attempt ->
                    if (acquired) return@repeat
                    try {
                        val jpAuthData = authRepository.refreshAuth(Region.JAPAN)
                        jpHelper = PurchaseHelper(jpAuthData).using(client) as PurchaseHelper
                        if (jpHelper.acquire(packageName, versionCode, offerType)) {
                            acquired = true
                            try {
                                return@withContext jpHelper.purchase(packageName, versionCode, offerType, certificateHash)
                            } catch (e2: Exception) {
                                lastPurchaseError = e2
                            }
                        } else {
                            val country = DfeCookieUtil.extractCountry(jpAuthData.dfeCookie)
                            lastAcquireError = "acquire() returned false (country=$country)"
                        }
                    } catch (e2: Exception) {
                        lastAcquireError = e2.message
                    }
                }

                throw IllegalStateException(
                    "This game is region-locked to Japan by Google Play. Please turn on a Japan VPN to download it for the first time on this network.",
                    e
                )
            } else {
                throw e
            }
        }
    }
}
