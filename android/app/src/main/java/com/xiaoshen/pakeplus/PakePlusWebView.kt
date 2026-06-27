package com.xiaoshen.pakeplus

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.CookieManager
import java.io.ByteArrayOutputStream
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.webkit.WebViewAssetLoader
import com.xiaoshen.pakeplus.util.AssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val JS_BRIDGE_NAME = "AndroidBridge"
private const val JS_BRIDGE_ADAPTER = """
    (function() {
        if (window.AndroidBridge && !window.webkit) {
            window.webkit = {
                messageHandlers: {
                    blobDownload: {
                        postMessage: function(msg) {
                            window.AndroidBridge.postMessage(JSON.stringify(msg));
                        }
                    }
                }
            };
        }
    })();
"""

private val VIEWPORT_SCRIPT = """
    (function() {
        var meta = document.createElement('meta');
        meta.name = 'viewport';
        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
        var existing = document.querySelector('meta[name=viewport]');
        if (existing) existing.parentNode.removeChild(existing);
        document.head.appendChild(meta);
    })();
""".trimIndent()

private val EXTERNAL_SCHEMES = setOf("tel", "mailto", "sms", "facetime", "facetime-audio")

private val DOWNLOADABLE_EXTENSIONS = setOf(
    "png", "jpg", "jpeg", "gif", "bmp", "webp", "heic",
    "mp4", "mov", "m4v", "avi", "mkv",
    "mp3", "wav", "aac", "m4a", "flac",
    "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
    "zip", "rar", "7z"
)

private data class BlobState(
    val filename: String,
    val mimeType: String,
    val totalChunks: Int,
    val received: MutableSet<Int> = mutableSetOf(),
    val buffer: ByteArrayOutputStream = ByteArrayOutputStream()
)

@Composable
fun PakePlusWebView(
    webUrl: String,
    debug: Boolean,
    userAgent: String,
    isHtml: Boolean,
    onLoadFinished: () -> Unit,
    onDownloadStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val blobDownloads = remember { mutableStateMapOf<String, BlobState>() }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    var pendingPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    var pendingGeolocationCallback by remember {
        mutableStateOf<((Boolean, Boolean) -> Unit)?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        pendingPermissionRequest?.let { request ->
            if (granted) request.grant(request.resources) else request.deny()
            pendingPermissionRequest = null
        }
        pendingGeolocationCallback?.let { callback ->
            callback(granted, true)
            pendingGeolocationCallback = null
        }
    }

    val assetLoader = remember(context) {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    val injectScripts: (WebView) -> Unit = remember(debug, context) {
        { webView ->
            webView.evaluateJavascript(JS_BRIDGE_ADAPTER, null)
            if (debug) {
                AssetLoader.loadAssetText(context, "vConsole.js")?.let { vConsole ->
                    webView.evaluateJavascript(vConsole, null)
                    webView.evaluateJavascript("var vConsole = new window.VConsole();", null)
                }
            }
            AssetLoader.loadAssetText(context, "custom.js")?.let {
                webView.evaluateJavascript(it, null)
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef.value = this
                initSettings(ctx, debug, userAgent)
                initBridge(this) { message ->
                    handleBlobMessage(
                        message,
                        blobDownloads,
                        scope,
                        context,
                        onDownloadStarted
                    )
                }
                initGesture(this)
                setDownloadListener { url, _, contentDisposition, mimetype, _ ->
                    enqueueDownload(context, url, contentDisposition, mimetype)
                    onDownloadStarted()
                }
                webViewClient = PakeWebViewClient(
                    context = ctx,
                    assetLoader = assetLoader,
                    onPageFinished = { webView, _ ->
                        injectScripts(webView)
                        webView.evaluateJavascript(VIEWPORT_SCRIPT, null)
                        onLoadFinished()
                    },
                    onReceivedError = { onLoadFinished() },
                    onDownloadStarted = onDownloadStarted
                )
                webChromeClient = PakeWebChromeClient(
                    onPermissionRequest = { request, permissions ->
                        pendingPermissionRequest = request
                        permissionLauncher.launch(permissions)
                    },
                    onGeolocationRequest = { callback ->
                        pendingGeolocationCallback = callback
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )

                if (isHtml || webUrl.isBlank()) {
                    loadUrl("https://appassets.androidplatform.net/assets/index.html")
                } else {
                    loadUrl(webUrl)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef.value?.apply {
                stopLoading()
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.initSettings(context: Context, debug: Boolean, userAgent: String) {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        allowFileAccess = false
        allowContentAccess = false
        allowFileAccessFromFileURLs = false
        allowUniversalAccessFromFileURLs = false
        loadWithOverviewMode = true
        useWideViewPort = true
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        mediaPlaybackRequiresUserGesture = false
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        cacheMode = WebSettings.LOAD_DEFAULT
        if (userAgent.isNotBlank()) {
            this.userAgentString = userAgent
        }
    }
    setBackgroundColor(android.graphics.Color.TRANSPARENT)
    isOpaque = false
    isHorizontalScrollBarEnabled = false
    isVerticalScrollBarEnabled = false
    WebView.setWebContentsDebuggingEnabled(debug)
    CookieManager.getInstance().setAcceptCookie(true)
}

private fun initBridge(webView: WebView, handler: (BlobDownloadBridge.Message) -> Unit) {
    webView.addJavascriptInterface(BlobDownloadBridge(handler), JS_BRIDGE_NAME)
}

private fun initGesture(webView: WebView) {
    val detector = GestureDetector(
        webView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val deltaX = e2.x - e1.x
                val deltaY = e2.y - e1.y
                if (kotlin.math.abs(deltaX) < kotlin.math.abs(deltaY)) return false
                if (kotlin.math.abs(deltaX) < 120 || kotlin.math.abs(velocityX) < 200) return false
                if (deltaX > 0 && webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
                if (deltaX < 0 && webView.canGoForward()) {
                    webView.goForward()
                    return true
                }
                return false
            }
        }
    )
    webView.setOnTouchListener { _, event ->
        detector.onTouchEvent(event)
        false
    }
}

private class PakeWebViewClient(
    private val context: Context,
    private val assetLoader: WebViewAssetLoader,
    private val onPageFinished: (WebView, String?) -> Unit,
    private val onReceivedError: () -> Unit,
    private val onDownloadStarted: () -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
            ?: super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        val url = request.url
        val scheme = url.scheme?.lowercase() ?: return false
        if (scheme in EXTERNAL_SCHEMES) {
            openExternalUrl(context, url)
            return true
        }
        if (request.isForMainFrame && shouldDownload(url)) {
            enqueueDownload(context, url.toString(), null, null)
            onDownloadStarted()
            return true
        }
        if (scheme !in setOf("http", "https")) {
            openExternalUrl(context, url)
            return true
        }
        return false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.let { onPageFinished(it, url) }
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        onReceivedError()
    }
}

private class PakeWebChromeClient(
    private val onPermissionRequest: (PermissionRequest, Array<String>) -> Unit,
    private val onGeolocationRequest: ((Boolean, Boolean) -> Unit) -> Unit
) : WebChromeClient() {

    override fun onPermissionRequest(request: PermissionRequest) {
        val permissions = request.resources.flatMap { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> listOf(Manifest.permission.CAMERA)
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> listOf(Manifest.permission.RECORD_AUDIO)
                else -> emptyList()
            }
        }.distinct().toTypedArray()
        if (permissions.isEmpty()) {
            request.deny()
            return
        }
        onPermissionRequest(request, permissions)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        onGeolocationRequest { allow, remember ->
            callback?.invoke(origin, allow, remember)
        }
    }
}

private fun shouldDownload(uri: Uri): Boolean {
    val ext = uri.path?.substringAfterLast('.', "")?.lowercase() ?: return false
    return ext in DOWNLOADABLE_EXTENSIONS
}

private fun openExternalUrl(context: Context, uri: Uri) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        // no handler available
    }
}

private fun enqueueDownload(
    context: Context,
    url: String,
    contentDisposition: String?,
    mimeType: String?
) {
    val filename = URLUtil.guessFileName(url, contentDisposition, mimeType) ?: "download"
    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setTitle(filename)
        setMimeType(mimeType ?: "*/*")
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
    }
    context.getSystemService<DownloadManager>()?.enqueue(request)
}

private fun handleBlobMessage(
    message: BlobDownloadBridge.Message,
    blobDownloads: MutableMap<String, BlobState>,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context,
    onDownloadStarted: () -> Unit
) {
    when (message.action) {
        "start" -> {
            val filename = sanitizeFilename(message.filename ?: "download")
            blobDownloads[message.id] = BlobState(
                filename = filename,
                mimeType = message.mimeType ?: "application/octet-stream",
                totalChunks = message.totalChunks
            )
            onDownloadStarted()
        }
        "chunk" -> {
            val state = blobDownloads[message.id] ?: return
            val index = message.index
            if (index < 0 || index >= state.totalChunks || index in state.received) return
            message.data?.let { base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    state.received.add(index)
                    state.buffer.write(bytes)
                } catch (_: IllegalArgumentException) {
                    // malformed chunk, ignore
                }
            }
        }
        "finish" -> {
            val state = blobDownloads.remove(message.id) ?: return
            if (state.received.size < state.totalChunks) return
            scope.launch(Dispatchers.IO) {
                saveBlobData(context, state.buffer.toByteArray(), state.filename, state.mimeType)
            }
        }
        "error" -> blobDownloads.remove(message.id)
    }
}

private suspend fun saveBlobData(
    context: Context,
    data: ByteArray,
    filename: String,
    mimeType: String
) {
    withContext(Dispatchers.IO) {
        try {
            val name = sanitizeFilename(filename)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = android.content.ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let { downloadUri ->
                    resolver.openOutputStream(downloadUri)?.use { it.write(data) }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(downloadUri, values, null, null)
                }
            } else {
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, name)
                file.writeBytes(data)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.download_finished, Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.download_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun sanitizeFilename(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "download"
    return trimmed.replace("/", "_").replace(":", "_").replace("\\", "_")
}
