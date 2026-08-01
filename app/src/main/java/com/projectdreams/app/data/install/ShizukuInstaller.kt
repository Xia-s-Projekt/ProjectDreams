package com.projectdreams.app.data.install

import android.os.ParcelFileDescriptor
import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

/**
 * Shizuku installer: executes the same `pm install-create -i com.android.vending`
 * session commands, but through the Shizuku server (shell/root privileges).
 *
 * Shizuku API 13.1.5 keeps `Shizuku.newProcess` private (deprecated for removal),
 * so it is invoked reflectively — a widely used, stable workaround.
 */
class ShizukuInstaller : Installer {

    override val name: String = "Shizuku"

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override suspend fun install(files: List<File>, totalBytes: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = Process.myUid() / 100_000
                val output = StringBuilder()

                val createOutput = runPm(
                    arrayOf(
                        "pm", "install-create", "-i", RootInstaller.PLAY_PACKAGE_NAME,
                        "--user", "$userId", "-r", "-S", "$totalBytes"
                    ),
                    output
                )
                val sessionId = SESSION_ID_PATTERN.find(createOutput)
                    ?.groupValues?.get(1)?.toIntOrNull()
                    ?: throw IllegalStateException("Failed to create install session: $createOutput")

                for (file in files) {
                    val writeOutput = runPm(
                        arrayOf(
                            "/system/bin/sh", "-c",
                            "cat '${file.absolutePath}' | " +
                                "pm install-write -S ${file.length()} $sessionId '${file.name}'"
                        ),
                        output
                    )
                    if (writeOutput.contains("Failure", ignoreCase = true)) {
                        runPm(arrayOf("pm", "install-abandon", "$sessionId"), output)
                        throw IllegalStateException("install-write failed for ${file.name}: $writeOutput")
                    }
                }

                val commitOutput = runPm(arrayOf("pm", "install-commit", "$sessionId"), output)
                if (commitOutput.contains("Success", ignoreCase = true)) {
                    Result.success(commitOutput)
                } else {
                    throw IllegalStateException("install-commit failed: $commitOutput")
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun uninstall(packageName: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val output = StringBuilder()
                val result = runPm(arrayOf("pm", "uninstall", "--user", "0", packageName), output)
                if (result.contains("Success", ignoreCase = true)) {
                    Result.success(result)
                } else {
                    Result.failure(IllegalStateException("pm uninstall failed: $result"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun setInstaller(
        packageName: String,
        installerPackage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val output = StringBuilder()
            val result = runPm(arrayOf("pm", "set-installer", packageName, installerPackage), output)
            if (result.contains("Success", ignoreCase = true)) {
                Result.success(result)
            } else {
                Result.failure(IllegalStateException("pm set-installer failed: $result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reinstallFromInstalled(
        packageName: String,
        installerPackage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val output = StringBuilder()
            val userId = Process.myUid() / 100_000
            val pathResult = runPm(arrayOf("pm", "path", packageName), output)
            val apks = pathResult.lines()
                .mapNotNull { line -> line.trim().removePrefix("package:").takeIf { it.endsWith(".apk") } }
                .map { path -> path to statSize(path, output) }
            check(apks.isNotEmpty()) { "pm path returned no APKs: $pathResult" }

            val createOutput = runPm(
                arrayOf(
                    "pm", "install-create", "-i", installerPackage,
                    "--user", "$userId", "-r", "-S", "${apks.sumOf { it.second }}"
                ),
                output
            )
            val sessionId = SESSION_ID_PATTERN.find(createOutput)
                ?.groupValues?.get(1)?.toIntOrNull()
                ?: throw IllegalStateException("Failed to create install session: $createOutput")

            for ((path, size) in apks) {
                val name = path.substringAfterLast('/')
                val writeOutput = runPm(
                    arrayOf(
                        "/system/bin/sh", "-c",
                        "cat '$path' | pm install-write -S $size $sessionId '$name'"
                    ),
                    output
                )
                if (writeOutput.contains("Failure", ignoreCase = true)) {
                    runPm(arrayOf("pm", "install-abandon", "$sessionId"), output)
                    throw IllegalStateException("install-write failed for $name: $writeOutput")
                }
            }

            val commitOutput = runPm(arrayOf("pm", "install-commit", "$sessionId"), output)
            if (commitOutput.contains("Success", ignoreCase = true)) {
                Result.success(commitOutput)
            } else {
                throw IllegalStateException("install-commit failed: $commitOutput")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun statSize(path: String, output: StringBuilder): Long =
        runPm(arrayOf("/system/bin/sh", "-c", "stat -c %s '$path'"), output)
            .trim().toLongOrNull() ?: 0L

    private fun runPm(cmd: Array<String>, accumulator: StringBuilder): String {
        val process = createProcess(cmd)
        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exit = process.waitFor()
        val combined = (stdout + "\n" + stderr).trim()
        if (combined.isNotBlank()) {
            accumulator.append(combined).append('\n')
        }
        Log.i(TAG, "shizuku ${cmd.joinToString(" ")} -> exit=$exit :: $combined")
        return combined
    }

    private fun createProcess(cmd: Array<String>): ShizukuRemoteProcess {
        val clazz = Class.forName("rikka.shizuku.Shizuku")
        val method = clazz.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(null, cmd, null, null) as ShizukuRemoteProcess
    }

    companion object {
        private const val TAG = "ShizukuInstaller"
        private val SESSION_ID_PATTERN = Regex("""\[(\d+)]""")
    }
}
