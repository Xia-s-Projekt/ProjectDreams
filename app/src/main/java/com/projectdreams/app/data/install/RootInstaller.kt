package com.projectdreams.app.data.install

import android.os.Process
import com.topjohnwu.superuser.Shell
import java.io.File

/**
 * Root installer: runs the real `pm` tool via libsu with `-i com.android.vending`
 * so the system records Google Play as the installing app (same trick Aurora Store
 * uses). Requires an app-granted root shell.
 */
class RootInstaller : Installer {

    override val name: String = "Root"

    override suspend fun isAvailable(): Boolean = try {
        Shell.getShell().isRoot
    } catch (_: Exception) {
        false
    }

    /**
     * Re-probes su with a fresh shell. libsu caches the main shell per process, and
     * a shell created before the user granted access in the root manager stays
     * non-root forever — killing it forces the next [Shell.cmd] call to rebuild.
     */
    suspend fun refresh(): Boolean = try {
        val cached = Shell.getShell()
        if (cached.isAlive) cached.close()
        Shell.getShell().isRoot
    } catch (_: Exception) {
        false
    }

    override suspend fun install(files: List<File>, totalBytes: Long): Result<String> {
        return try {
            val userId = Process.myUid() / 100_000
            val create = Shell.cmd(
                "pm install-create -i $PLAY_PACKAGE_NAME --user $userId -r -S $totalBytes"
            ).exec()

            val sessionId = SESSION_ID_PATTERN.find(create.out.joinToString(" "))
                ?.groupValues?.get(1)?.toIntOrNull()
                ?: throw IllegalStateException(
                    "Failed to create install session: ${create.out.joinToString(" ")}"
                )

            for (file in files) {
                val write = Shell.cmd(
                    "cat \"${file.absolutePath}\" | pm install-write -S ${file.length()} " +
                        "$sessionId \"${file.name}\""
                ).exec()
                if (!write.isSuccess) {
                    Shell.cmd("pm install-abandon $sessionId").exec()
                    throw IllegalStateException(
                        "install-write failed for ${file.name}: ${write.err.joinToString("\n")}"
                    )
                }
            }

            val commit = Shell.cmd("pm install-commit $sessionId").exec()
            val output = commit.out.joinToString("\n").ifBlank { commit.err.joinToString("\n") }
            if (commit.isSuccess && output.contains("Success", ignoreCase = true)) {
                Result.success(output)
            } else {
                throw IllegalStateException("install-commit failed: $output")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uninstall(packageName: String): Result<String> = try {
        val result = Shell.cmd("pm uninstall --user 0 $packageName").exec()
        val output = result.out.joinToString("\n").ifBlank { result.err.joinToString("\n") }
        if (result.isSuccess && output.contains("Success", ignoreCase = true)) {
            Result.success(output)
        } else {
            Result.failure(IllegalStateException("pm uninstall failed: $output"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun setInstaller(
        packageName: String,
        installerPackage: String
    ): Result<String> = try {
        val result = Shell.cmd("pm set-installer $packageName $installerPackage").exec()
        val output = result.out.joinToString("\n").ifBlank { result.err.joinToString("\n") }
        if (result.isSuccess && output.contains("Success", ignoreCase = true)) {
            Result.success(output)
        } else {
            Result.failure(IllegalStateException("pm set-installer failed: $output"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun reinstallFromInstalled(
        packageName: String,
        installerPackage: String
    ): Result<String> {
        return try {
            val userId = Process.myUid() / 100_000
            val pathResult = Shell.cmd("pm path $packageName").exec()
            val apks = pathResult.out
                .mapNotNull { line ->
                    line.trim().removePrefix("package:").takeIf { it.endsWith(".apk") }
                }
                .map { path -> path to statSize(path) }
            check(apks.isNotEmpty()) { "pm path returned no APKs: ${pathResult.out.joinToString(" ")}" }

            val create = Shell.cmd(
                "pm install-create -i $installerPackage --user $userId -r -S ${apks.sumOf { it.second }}"
            ).exec()
            val sessionId = SESSION_ID_PATTERN.find(create.out.joinToString(" "))
                ?.groupValues?.get(1)?.toIntOrNull()
                ?: throw IllegalStateException(
                    "Failed to create install session: ${create.out.joinToString(" ")}"
                )

            for ((path, size) in apks) {
                val name = path.substringAfterLast('/')
                val write = Shell.cmd(
                    "cat \"$path\" | pm install-write -S $size $sessionId \"$name\""
                ).exec()
                if (!write.isSuccess) {
                    Shell.cmd("pm install-abandon $sessionId").exec()
                    throw IllegalStateException(
                        "install-write failed for $name: ${write.err.joinToString("\n")}"
                    )
                }
            }

            val commit = Shell.cmd("pm install-commit $sessionId").exec()
            val output = commit.out.joinToString("\n").ifBlank { commit.err.joinToString("\n") }
            if (commit.isSuccess && output.contains("Success", ignoreCase = true)) {
                Result.success(output)
            } else {
                throw IllegalStateException("install-commit failed: $output")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun statSize(path: String): Long =
        Shell.cmd("stat -c %s \"$path\"").exec().out.joinToString(" ").trim().toLongOrNull() ?: 0L

    companion object {
        const val PLAY_PACKAGE_NAME = "com.android.vending"
        private val SESSION_ID_PATTERN = Regex("""\[(\d+)]""")
    }
}
