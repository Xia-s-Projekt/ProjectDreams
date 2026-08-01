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

    suspend fun purchase(
        packageName: String,
        versionCode: Long,
        offerType: Int,
        certificateHash: String? = null
    ): List<PlayFile> = withContext(Dispatchers.IO) {
        val authData = authRepository.authData()
        PurchaseHelper(authData)
            .using(client)
            .purchase(packageName, versionCode, offerType, certificateHash)
    }
}
