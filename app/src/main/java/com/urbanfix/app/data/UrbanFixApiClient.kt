package com.urbanfix.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val isLive: Boolean = true) : ApiResult<T>()
    data class Error(val message: String, val isOffline: Boolean = false) : ApiResult<Nothing>()
}

/**
 * Week 7 — API Integration & Offline Reliability
 * Connects to httpbin / mock public endpoint for syncing reports and testing network reliability.
 */
class UrbanFixApiClient(private val context: Context) {

    private val apiUrl = "https://httpbin.org/post"
    private val pingUrl = "https://httpbin.org/get"

    suspend fun syncTicket(ticket: Ticket): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("action", "SYNC_REPORT")
                put("ticket", ticket.toJson())
                put("app", "UrbanFix Lodz")
            }.toString()

            OutputStreamWriter(conn.outputStream).use { it.write(payload) }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val resp = reader.readText()
                reader.close()
                ApiResult.Success("Synced with city server (HTTP $responseCode)")
            } else {
                ApiResult.Error("Server returned code $responseCode")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network unreachable", isOffline = true)
        }
    }

    suspend fun checkApiHealth(): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(pingUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000

            val code = conn.responseCode
            if (code == 200) {
                ApiResult.Success("City Infrastructure API Online")
            } else {
                ApiResult.Error("API status error: $code")
            }
        } catch (e: Exception) {
            ApiResult.Error("Offline mode: changes saved locally", isOffline = true)
        }
    }
}
