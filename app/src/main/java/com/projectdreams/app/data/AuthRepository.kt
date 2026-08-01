package com.projectdreams.app.data

import android.content.Context
import android.util.Log
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.serializers.LocaleSerializer
import com.aurora.gplayapi.data.serializers.PropertiesSerializer
import com.aurora.gplayapi.helpers.AuthHelper
import com.projectdreams.app.R
import java.util.Locale
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@Serializable
data class Auth(
    val email: String,
    @SerialName("authToken") val auth: String
)

/**
 * Builds and persists an anonymous Google Play session, exactly like Aurora Store does:
 * the device profile is POSTed to the AuroraOSS token dispenser which returns an
 * anonymous auth token, and [AuthHelper] turns it into an [AuthData] session.
 * Sessions are cached per [Region] so GL and JP storefronts stay independent.
 */
class AuthRepository(
    private val context: Context,
    private val client: PlayHttpClient,
    private val settingsRepository: SettingsRepository
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        serializersModule = SerializersModule {
            contextual(LocaleSerializer)
            contextual(PropertiesSerializer)
        }
    }

    private val baseDeviceProperties: Properties by lazy {
        context.resources.openRawResource(R.raw.device).use { stream ->
            Properties().apply { load(stream) }
        }
    }

    private fun devicePropertiesFor(region: Region): Properties {
        val props = Properties()
        props.putAll(baseDeviceProperties)
        if (region == Region.JAPAN) {
            props["TimeZone"] = "Asia/Tokyo"
        }
        return props
    }

    private fun localeFor(region: Region): Locale =
        if (region == Region.JAPAN) Locale.JAPAN else Locale.getDefault()

    /**
     * Returns a valid session for [region], refreshing it from the dispenser when
     * the cached one is missing or expired.
     */
    suspend fun authData(region: Region = settingsRepository.region.value): AuthData =
        withContext(Dispatchers.IO) {
            prefs.getString(authKey(region), null)?.let { cached ->
                runCatching {
                    val authData = json.decodeFromString<AuthData>(cached)
                    if (AuthHelper.using(client).isValid(authData)) {
                        return@withContext authData
                    }
                }
            }
            fetchAndSave(region)
        }

    private suspend fun fetchAndSave(region: Region): AuthData = withContext(Dispatchers.IO) {
        val props = devicePropertiesFor(region)
        val response = client.postAuth(
            DISPENSER_URL,
            json.encodeToString(props).toByteArray()
        )
        if (!response.isSuccessful) {
            throw IllegalStateException(
                "Token dispenser error (${response.code}): ${response.errorString.ifBlank { "unknown" }}"
            )
        }
        val auth = json.decodeFromString<Auth>(String(response.responseBytes))
        val authData = AuthHelper.build(
            email = auth.email,
            token = auth.auth,
            tokenType = AuthHelper.Token.AUTH,
            isAnonymous = true,
            properties = props,
            locale = localeFor(region)
        )
        prefs.edit().putString(authKey(region), json.encodeToString(authData)).apply()
        Log.i(TAG, "Fetched new anonymous session (${region.name}) for ${auth.email}")
        authData
    }

    private fun authKey(region: Region) = "${KEY_AUTH_DATA}_${region.name}"

    companion object {
        private const val TAG = "AuthRepository"
        private const val PREFS_NAME = "session"
        private const val KEY_AUTH_DATA = "auth_data"
        private const val DISPENSER_URL = "https://auroraoss.com/api/auth"
    }
}
