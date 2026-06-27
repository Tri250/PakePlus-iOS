package com.xiaoshen.pakeplus

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.webkit.WebViewAssetLoader
import com.xiaoshen.pakeplus.data.WebViewConfig
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
    isHtml: Boolean,
    onLoadFinished: () -> Unit,
    onDownloadStarted: () -> Unit,
    userAgent: String = "",
    webViewConfig: WebViewConfig? = null,
    reloadSignal: Int = 0,
    onUrlChanged: (String) -> Unit = {},
    isWebLoaded: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val blobDownloads = remember { mutableStateMapOf<String, BlobState>() }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var initialLoadDone by remember { mutableStateOf(false) }

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

    // Scripts injected at document start (matching iOS atDocumentStart behavior)
    val injectEarlyScripts: (WebView) -> Unit = remember(debug, context) {
        { webView ->
            // Bridge adapter must be available before page JS runs
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
                initSettings(ctx, debug, userAgent, webViewConfig)
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
                    onPageStarted = { webView, url ->
                        url?.let { onUrlChanged(it) }
                        // Inject scripts at document start (iOS atDocumentStart equivalent)
                        injectEarlyScripts(webView)
                    },
                    onPageFinished = { webView, _ ->
                        // Only inject viewport meta at document end (iOS atDocumentEnd equivalent)
                        webView.evaluateJavascript(VIEWPORT_SCRIPT, null)
                        onLoadFinished()
                    },
                    onReceivedError = { onLoadFinished() },
                    onDownloadStarted = onDownloadStarted
                )
                webChromeClient = PakeWebChromeClient(
                    context = ctx,
                    onPermissionRequest = { request, permissions ->
                        // Check if all permissions already granted before showing dialog
                        val allGranted = permissions.all {
                            ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED
                        }
                        if (allGranted) {
                            request.grant(request.resources)
                        } else {
                            pendingPermissionRequest = request
                            permissionLauncher.launch(permissions)
                        }
                    },
                    onGeolocationRequest = { callback ->
                        // Pre-check: if location permission already granted, auto-allow
                        val fineGranted = ContextCompat.checkSelfPermission(
                            ctx, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (fineGranted || coarseGranted) {
                            callback(true, true)
                        } else {
                            pendingGeolocationCallback = callback
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                // Pre-request geolocation authorization (matching iOS prepareWebGeolocationAuthorization)
                prepareGeolocationAuthorization(ctx, permissionLauncher)

                loadCurrentUrl(webUrl, isHtml, ctx)
            }
        },
        modifier = modifier.fillMaxSize()
    )

    LaunchedEffect(webUrl, isHtml) {
        if (initialLoadDone) {
            webViewRef.value?.loadCurrentUrl(webUrl, isHtml, context)
        } else {
            initialLoadDone = true
        }
    }

    LaunchedEffect(reloadSignal) {
        if (reloadSignal > 0) {
            webViewRef.value?.reload()
        }
    }

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
private fun WebView.initSettings(
    context: Context,
    debug: Boolean,
    userAgent: String,
    webViewConfig: WebViewConfig?
) {
    settings.apply {
        javaScriptEnabled = webViewConfig?.javaScriptEnabled ?: true
        domStorageEnabled = webViewConfig?.domStorageEnabled ?: true
        databaseEnabled = true
        allowFileAccess = webViewConfig?.allowFileAccess ?: false
        allowContentAccess = false
        allowFileAccessFromFileURLs = false
        allowUniversalAccessFromFileURLs = false
        loadWithOverviewMode = webViewConfig?.loadWithOverviewMode ?: true
        useWideViewPort = true
        setSupportZoom(webViewConfig?.setSupportZoom ?: false)
        builtInZoomControls = false
        displayZoomControls = false
        mediaPlaybackRequiresUserGesture = false
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        cacheMode = WebSettings.LOAD_DEFAULT
        val effectiveUserAgent = userAgent.takeIf { it.isNotBlank() }
            ?: webViewConfig?.userAgent?.takeIf { it.isNotBlank() } ?: ""
        if (effectiveUserAgent.isNotBlank()) {
            this.userAgentString = effectiveUserAgent
        }
    }
    setBackgroundColor(android.graphics.Color.TRANSPARENT)
    isOpaque = false
    isHorizontalScrollBarEnabled = false
    isVerticalScrollBarEnabled = false
    WebView.setWebContentsDebuggingEnabled(debug)
    CookieManager.getInstance().setAcceptCookie(true)
    if (webViewConfig?.clearCache == true) {
        clearCache(true)
    }
}

private fun WebView.loadCurrentUrl(webUrl: String, isHtml: Boolean, context: Context) {
    if (isHtml || webUrl.isBlank()) {
        loadUrl("https://appassets.androidplatform.net/assets/index.html")
        return
    }
    // URL host routing (matching iOS behavior)
    val uri = Uri.parse(webUrl)
    val host = uri.host?.lowercase() ?: ""
    when {
        host.contains("pakeplus.com") -> {
            // Load local HTML for pakeplus.com host (matching iOS)
            loadUrl("https://appassets.androidplatform.net/assets/index.html")
        }
        host.contains("password.com") -> {
            // Load password entry page (matching iOS)
            loadUrl("https://appassets.androidplatform.net/assets/index.html")
        }
        else -> loadUrl(webUrl)
    }
}

/**
 * Pre-request geolocation authorization at WebView creation time,
 * matching iOS prepareWebGeolocationAuthorization() behavior.
 */
private fun prepareGeolocationAuthorization(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val fineGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!fineGranted && !coarseGranted) {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
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
    private val onPageStarted: (WebView, String?) -> Unit,
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

    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.let { onPageStarted(it, url) }
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
    private val context: Context,
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
