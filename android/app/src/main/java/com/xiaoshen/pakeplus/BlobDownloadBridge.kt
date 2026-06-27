package com.xiaoshen.pakeplus

import android.webkit.JavascriptInterface
import org.json.JSONObject

class BlobDownloadBridge(private val handler: (Message) -> Unit) {

    data class Message(
        val action: String,
        val id: String,
        val filename: String? = null,
        val mimeType: String? = null,
        val totalChunks: Int = 1,
        val index: Int = -1,
        val data: String? = null,
        val errorMessage: String? = null
    )

    @JavascriptInterface
    fun postMessage(json: String) {
        try {
            val obj = JSONObject(json)
            handler(
                Message(
                    action = obj.optString("action", ""),
                    id = obj.optString("id", ""),
                    filename = obj.optString("filename", null),
                    mimeType = obj.optString("mimeType", null),
                    totalChunks = obj.optInt("totalChunks", 1).coerceAtLeast(1),
                    index = obj.optInt("index", -1),
                    data = obj.optString("data", null),
                    errorMessage = obj.optString("message", null)
                )
            )
        } catch (_: Exception) {
            // ignore malformed messages
        }
    }
}
