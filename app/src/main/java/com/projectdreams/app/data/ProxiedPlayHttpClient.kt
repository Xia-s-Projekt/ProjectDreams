package com.projectdreams.app.data

import com.aurora.gplayapi.data.models.PlayResponse
import com.aurora.gplayapi.network.IHttpClient
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * An [IHttpClient] that routes Google Play API requests (`fdfe/acquire`,
 * `fdfe/purchase`, `fdfe/delivery`) through a SOCKS5 proxy so the server
 * sees a Japan IP, while all other traffic (CDN downloads, token dispenser)
 * goes direct.
 *
 * This is the mechanism that actually bypasses Google Play's IP-based
 * region lock on the acquire step for never-before-purchased apps.
 */
class ProxiedPlayHttpClient(
    private val directClient: OkHttpClient,
    proxyHost: String,
    proxyPort: Int,
    proxyType: Proxy.Type = Proxy.Type.SOCKS
) : IHttpClient {

    private val proxiedClient: OkHttpClient = directClient.newBuilder()
        .proxy(Proxy(proxyType, InetSocketAddress.createUnresolved(proxyHost, proxyPort)))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _responseCode = MutableStateFlow(100)
    override val responseCode: StateFlow<Int>
        get() = _responseCode.asStateFlow()

    /** Returns the proxied client for Google Play API calls, direct for everything else. */
    private fun clientFor(url: String): OkHttpClient =
        if (PROXIED_PATHS.any { url.contains(it) }) proxiedClient else directClient

    @Throws(IOException::class)
    override fun post(
        url: String,
        headers: Map<String, String>,
        body: ByteArray
    ): PlayResponse {
        val request = Request(
            url = url.toHttpUrl(),
            headers = headers.toHeaders(),
            method = POST,
            body = body.toRequestBody()
        )
        return processRequest(request, clientFor(url))
    }

    @Throws(IOException::class)
    override fun post(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): PlayResponse {
        val request = Request(
            url = buildUrl(url, params),
            headers = headers.toHeaders(),
            method = POST,
            body = "".toRequestBody(null)
        )
        return processRequest(request, clientFor(url))
    }

    override fun postAuth(url: String, body: ByteArray): PlayResponse {
        val headers = mapOf("User-Agent" to AURORA_USER_AGENT)
        val requestBody = body.toRequestBody("application/json".toMediaType(), 0, body.size)
        val request = Request(
            url = url.toHttpUrl(),
            headers = headers.toHeaders(),
            method = POST,
            body = requestBody
        )
        // Auth calls go to the Aurora dispenser, not Google — always direct.
        return processRequest(request, directClient)
    }

    @Throws(IOException::class)
    override fun get(url: String, headers: Map<String, String>): PlayResponse =
        get(url, headers, mapOf())

    @Throws(IOException::class)
    override fun get(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): PlayResponse {
        val request = Request(
            url = buildUrl(url, params),
            headers = headers.toHeaders(),
            method = GET
        )
        return processRequest(request, clientFor(url))
    }

    override fun getAuth(url: String): PlayResponse {
        val headers = mapOf(
            "User-Agent" to "${com.projectdreams.app.BuildConfig.APPLICATION_ID}-${com.projectdreams.app.BuildConfig.VERSION_NAME}-${com.projectdreams.app.BuildConfig.VERSION_CODE}"
        )
        val request = Request(
            url = url.toHttpUrl(),
            headers = headers.toHeaders(),
            method = GET
        )
        return processRequest(request, directClient)
    }

    @Throws(IOException::class)
    override fun get(url: String, headers: Map<String, String>, paramString: String): PlayResponse {
        val request = Request(
            url = "$url$paramString".toHttpUrl(),
            headers = headers.toHeaders(),
            method = GET
        )
        return processRequest(request, clientFor(url))
    }

    private fun processRequest(request: Request, client: OkHttpClient): PlayResponse {
        _responseCode.value = 0
        return buildPlayResponse(client.newCall(request).execute())
    }

    private fun buildUrl(url: String, params: Map<String, String>): HttpUrl {
        val urlBuilder = url.toHttpUrl().newBuilder()
        params.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        return urlBuilder.build()
    }

    private fun buildPlayResponse(response: Response): PlayResponse = PlayResponse(
        isSuccessful = response.isSuccessful,
        code = response.code,
        responseBytes = response.body.bytes(),
        errorString = if (!response.isSuccessful) response.message else String()
    ).also {
        _responseCode.value = response.code
    }

    companion object {
        private const val POST = "POST"
        private const val GET = "GET"
        private const val AURORA_USER_AGENT = "com.aurora.store-4.9.1-30042"

        /** URL path fragments that must go through the Japan proxy. */
        private val PROXIED_PATHS = listOf(
            "fdfe/acquire",
            "fdfe/purchase",
            "fdfe/delivery"
        )
    }
}
