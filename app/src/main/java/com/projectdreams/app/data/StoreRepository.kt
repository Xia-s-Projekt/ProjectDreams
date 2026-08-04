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
        } catch (e: com.aurora.gplayapi.exceptions.GooglePlayException.AppNotPurchased) {
            val acquired = helper.acquire(packageName, versionCode, offerType)
            if (acquired) {
                helper.purchase(packageName, versionCode, offerType, certificateHash)
            } else {
                throw e
            }
        }
    }
}
