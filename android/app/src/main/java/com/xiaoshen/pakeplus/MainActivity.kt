package com.xiaoshen.pakeplus

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.xiaoshen.pakeplus.ui.components.DownloadHint
import com.xiaoshen.pakeplus.ui.components.LaunchOverlay
import com.xiaoshen.pakeplus.ui.theme.PakePlusTheme
import com.xiaoshen.pakeplus.util.ConfigParser

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val config = readManifestConfig()

        // Keep screen on
        if (config.screenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // Fullscreen / edge-to-edge
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (config.fullscreen) {
            hideSystemBars()
        }

        splashScreen.setKeepOnScreenCondition { !splashReady }

        setContent {
            PakePlusTheme {
                var isWebLoaded by remember { mutableStateOf(false) }
                var downloadHintVisible by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    PakePlusWebView(
                        webUrl = config.webUrl,
                        debug = config.debug,
                        userAgent = config.userAgent,
                        isHtml = config.isHtml,
                        onLoadFinished = {
                            isWebLoaded = true
                            splashReady = true
                        },
                        onDownloadStarted = {
                            downloadHintVisible = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    LaunchOverlay(
                        visible = !isWebLoaded && config.launchImage,
                        modifier = Modifier.fillMaxSize()
                    )

                    DownloadHint(
                        visible = downloadHintVisible,
                        message = getString(R.string.download_started),
                        modifier = Modifier.fillMaxSize(),
                        onDismiss = { downloadHintVisible = false }
                    )
                }
            }
        }
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun readManifestConfig(): ManifestConfig {
        val metaData = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData
        return ConfigParser.parse(metaData)
    }

    companion object {
        private var splashReady = false
    }
}

data class ManifestConfig(
    val webUrl: String,
    val debug: Boolean,
    val fullscreen: Boolean,
    val launchImage: Boolean,
    val screenOn: Boolean,
    val userAgent: String,
    val isHtml: Boolean
)
