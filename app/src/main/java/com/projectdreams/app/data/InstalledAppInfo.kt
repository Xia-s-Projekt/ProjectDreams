package com.projectdreams.app.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64

/**
 * Helpers for reading state of an installed package from the device.
 */
object InstalledAppInfo {

    fun installedVersion(context: Context, packageName: String): PackageInfo? = try {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
    } catch (_: Exception) {
        null
    }

    fun isInstalled(context: Context, packageName: String): Boolean =
        installedVersion(context, packageName) != null

    /** The package that installed [packageName] (e.g. "com.android.vending"), or
     *  null when unknown/not installed. Readable without any permissions. */
    fun installerPackage(context: Context, packageName: String): String? = try {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val info = pm.getInstallSourceInfo(packageName)
            info.installingPackageName?.let { return it }
            // The API 36 stubs dropped getInstallerPackageName, but it still exists
            // at runtime and is the authoritative "installed by" value.
            try {
                info.javaClass.getMethod("getInstallerPackageName").invoke(info) as? String
            } catch (_: Exception) {
                null
            }
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(packageName)
        }
    } catch (_: Exception) {
        null
    }

    /** SHA-256 base64 URL-safe hash of the installed app's signing certificate. */
    fun certificateHash(context: Context, packageName: String): String? {
        val installed = installedVersion(context, packageName) ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return try {
            installed.signingInfo
                ?.let {
                    if (it.hasMultipleSigners()) it.apkContentsSigners else it.signingCertificateHistory
                }
                ?.map { cert ->
                    Base64.encodeToString(
                        cert.toByteArray(),
                        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                    )
                }
                ?.lastOrNull()
        } catch (_: Exception) {
            null
        }
    }
}
