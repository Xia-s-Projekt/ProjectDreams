package com.projectdreams.app.data

import android.content.Context
import com.aurora.gplayapi.data.models.PlayFile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the purchased download manifest (file list with signed URLs) for a
 * (package, versionCode) session, so a retry or app restart can resume without
 * re-purchasing from Google Play (which rate-limits repeated purchase requests)
 * and without losing the "Continue" state.
 */
class DownloadStateStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(packageName: String, versionCode: Long, files: List<PlayFile>) {
        val array = JSONArray()
        files.forEach { file ->
            array.put(
                JSONObject()
                    .put("id", file.id)
                    .put("name", file.name)
                    .put("url", file.url)
                    .put("size", file.size)
                    .put("type", file.type.name)
                    .put("sha1", file.sha1)
                    .put("sha256", file.sha256)
            )
        }
        prefs.edit().putString(key(packageName, versionCode), array.toString()).apply()
    }

    fun loadFiles(packageName: String, versionCode: Long): List<PlayFile>? {
        val raw = prefs.getString(key(packageName, versionCode), null) ?: return null
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                PlayFile(
                    id = obj.optString("id"),
                    name = obj.getString("name"),
                    url = obj.getString("url"),
                    size = obj.getLong("size"),
                    type = runCatching { PlayFile.Type.valueOf(obj.optString("type")) }
                        .getOrDefault(PlayFile.Type.BASE),
                    sha1 = obj.optString("sha1"),
                    sha256 = obj.optString("sha256")
                )
            }
        }.getOrNull()
    }

    fun clear(packageName: String, versionCode: Long) {
        prefs.edit().remove(key(packageName, versionCode)).apply()
    }

    fun clearAll(packageName: String) {
        val prefix = "manifest_${packageName}_"
        prefs.edit().apply {
            prefs.all.keys.filter { it.startsWith(prefix) }.forEach { remove(it) }
            apply()
        }
    }

    private fun key(packageName: String, versionCode: Long) = "manifest_${packageName}_$versionCode"

    companion object {
        private const val PREFS_NAME = "download_state"
    }
}
