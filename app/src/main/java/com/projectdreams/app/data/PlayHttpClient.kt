package com.projectdreams.app.data

import com.aurora.gplayapi.data.models.PlayResponse
import com.aurora.gplayapi.network.IHttpClient
import com.projectdreams.app.BuildConfig
import java.io.IOException
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
 * OkHttp backed implementation of GPlayApi's [IHttpClient].
 */
class PlayHttpClient(private val okHttpClient: OkHttpClient) : IHttpClient {

    private val _responseCode = MutableStateFlow(100)
    override val responseCode: StateFlow<Int>
        get() = _responseCode.asStateFlow()

    @Throws(IOException::class)
    fun post(url: String, headers: Map<String, String>, requestBody: RequestBody): PlayResponse {
        val request = Request(
            url = url.toHttpUrl(),
            headers = headers.toHeaders(),
            method = POST,
            body = requestBody
        )
        return processRequest(request)
    }

    @Throws(IOException::class)
    fun call(url: String, headers: Map<String, String> = emptyMap()): Response {
        val request = Request(
            url = url.toHttpUrl(),
            headers = headers.toHeaders()
        )
        return okHttpClient.newCall(request).execute()
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
        return processRequest(request)
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
        return processRequest(request)
    }

    @Throws(IOException::class)
    override fun post(url: String, headers: Map<String, String>, body: ByteArray): PlayResponse =
        post(url, headers, body.toRequestBody())

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
        return processRequest(request)
    }

    override fun getAuth(url: String): PlayResponse {
        val headers = mapOf("User-Agent" to "${BuildConfig.APPLICATION_ID}-${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}")
        val request = Request(
            url = url.toHttpUrl(),
            headers = headers.toHeaders(),
            method = GET
        )
        return processRequest(request)
    }

    @Throws(IOException::class)
    override fun get(url: String, headers: Map<String, String>, paramString: String): PlayResponse {
        val request = Request(
            url = "$url$paramString".toHttpUrl(),
            headers = headers.toHeaders(),
            method = GET
        )
        return processRequest(request)
    }

    private fun processRequest(request: Request): PlayResponse {
        _responseCode.value = 0
        return buildPlayResponse(okHttpClient.newCall(request).execute())
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
    }
}
