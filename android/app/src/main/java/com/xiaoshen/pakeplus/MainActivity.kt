package com.xiaoshen.pakeplus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.xiaoshen.pakeplus.data.AppConfig
import com.xiaoshen.pakeplus.data.TabBarItem
import com.xiaoshen.pakeplus.ui.components.BottomTabBar
import com.xiaoshen.pakeplus.ui.components.DownloadHint
import com.xiaoshen.pakeplus.ui.components.LaunchOverlay
import com.xiaoshen.pakeplus.ui.components.SideDrawer
import com.xiaoshen.pakeplus.ui.components.TopHeader
import com.xiaoshen.pakeplus.ui.components.TopPopupMenu
import com.xiaoshen.pakeplus.ui.components.TopPopupMenuButton
import com.xiaoshen.pakeplus.ui.theme.PakePlusTheme
import com.xiaoshen.pakeplus.util.ConfigLoader
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val config = ConfigLoader.load(this)

        // Keep screen on
        if (config.phone.screenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // Fullscreen / edge-to-edge
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (config.phone.fullScreen) {
            hideSystemBars()
        }

        splashScreen.setKeepOnScreenCondition { !splashReady }

        setContent {
            PakePlusTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                var isWebLoaded by remember { mutableStateOf(false) }
                var downloadHintVisible by remember { mutableStateOf(false) }
                var menuExpanded by remember { mutableStateOf(false) }
                var reloadSignal by remember { mutableIntStateOf(0) }

                val tabBar = config.phone.tabBar
                val siderMenu = config.phone.siderMenu
                val header = config.phone.header

                val tabs = remember(tabBar.tabBarItem, config.android.webUrl) {
                    tabBar.tabBarItem.takeIf { it.isNotEmpty() } ?: listOf(
                        TabBarItem(title = "首页", icon = "home", url = config.android.webUrl),
                        TabBarItem(title = "收藏", icon = "star", url = config.android.webUrl),
                        TabBarItem(title = "我的", icon = "play", url = config.android.webUrl)
                    )
                }
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val initialUrl = if (tabBar.show) {
                    tabs.getOrElse(0) { TabBarItem(url = config.android.webUrl) }.url
                } else {
                    config.android.webUrl
                }
                var targetUrl by remember { mutableStateOf(initialUrl) }
                var displayedUrl by remember { mutableStateOf(initialUrl) }

                val safeArea = config.phone.safeArea
                val safeAreaModifier = Modifier.padding(
                    start = safeArea.left.dp,
                    end = safeArea.right.dp,
                    top = safeArea.top.dp,
                    bottom = safeArea.bottom.dp
                )

                val webViewModifier = Modifier.fillMaxSize()

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        tabBar.show -> {
                            SideDrawer(
                                drawerState = drawerState,
                                title = siderMenu.title.takeIf { it.isNotBlank() } ?: "左侧菜单"
                            ) {
                                Scaffold(
                                    modifier = safeAreaModifier.fillMaxSize(),
                                    topBar = {
                                    TopHeader(
                                        title = header.title.takeIf { it.isNotBlank() } ?: "PakePlus",
                                        config = header,
                                        onMenuClick = {
                                            scope.launch { drawerState.open() }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        actions = {
                                            if (header.toolBar) {
                                                TopPopupMenuButton(
                                                    expanded = menuExpanded,
                                                    onToggle = { menuExpanded = !menuExpanded }
                                                )
                                                TopPopupMenu(
                                                    expanded = menuExpanded,
                                                    currentUrl = displayedUrl,
                                                    onDismiss = { menuExpanded = false },
                                                    onCopyUrl = { url -> copyUrl(context, url) },
                                                    onOpenExternal = { url -> openExternalUrl(context, url) },
                                                    onReload = { reloadSignal++ }
                                                )
                                            }
                                        }
                                    )
                                },
                                bottomBar = {
                                    BottomTabBar(
                                            config = tabBar,
                                            selectedIndex = selectedTabIndex,
                                            onTabSelected = { index ->
                                                selectedTabIndex = index
                                                val tabUrl = tabs.getOrElse(index) { TabBarItem(url = config.android.webUrl) }.url
                                                targetUrl = tabUrl
                                                displayedUrl = tabUrl
                                                isWebLoaded = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                ) { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                        AppWebView(
                                            targetUrl = targetUrl,
                                            onUrlChanged = { displayedUrl = it },
                                            config = config,
                                            reloadSignal = reloadSignal,
                                            onWebLoaded = {
                                                isWebLoaded = true
                                                splashReady = true
                                            },
                                            onDownloadStarted = { downloadHintVisible = true },
                                            modifier = webViewModifier
                                        )
                                    }
                                }
                            }
                        }

                        siderMenu.show -> {
                            SideDrawer(
                                drawerState = drawerState,
                                title = siderMenu.title.takeIf { it.isNotBlank() } ?: "左侧菜单"
                            ) {
                                Column(
                                    modifier = safeAreaModifier
                                        .fillMaxSize()
                                        .statusBarsPadding()
                                ) {
                                    TopHeader(
                                        title = header.title.takeIf { it.isNotBlank() } ?: "PakePlus",
                                        config = header,
                                        onMenuClick = {
                                            scope.launch { drawerState.open() }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    AppWebView(
                                        targetUrl = targetUrl,
                                        onUrlChanged = { displayedUrl = it },
                                        config = config,
                                        reloadSignal = reloadSignal,
                                        onWebLoaded = {
                                            isWebLoaded = true
                                            splashReady = true
                                        },
                                        onDownloadStarted = { downloadHintVisible = true },
                                        modifier = webViewModifier
                                    )
                                }
                            }
                        }

                        else -> {
                            AppWebView(
                                targetUrl = targetUrl,
                                onUrlChanged = { displayedUrl = it },
                                config = config,
                                reloadSignal = reloadSignal,
                                onWebLoaded = {
                                    isWebLoaded = true
                                    splashReady = true
                                },
                                onDownloadStarted = { downloadHintVisible = true },
                                modifier = webViewModifier
                            )
                        }
                    }

                    LaunchOverlay(
                        visible = !isWebLoaded && config.phone.launchImage.isNotBlank(),
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

    private fun copyUrl(context: Context, url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("URL", url))
        Toast.makeText(context, "已复制网址", Toast.LENGTH_SHORT).show()
    }

    private fun openExternalUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private var splashReady = false
    }
}

@Composable
private fun AppWebView(
    targetUrl: String,
    onUrlChanged: (String) -> Unit,
    config: AppConfig,
    reloadSignal: Int,
    onWebLoaded: () -> Unit,
    onDownloadStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    PakePlusWebView(
        webUrl = targetUrl,
        debug = config.android.debug,
        isHtml = config.android.isHtml,
        webViewConfig = config.phone.webview,
        reloadSignal = reloadSignal,
        onLoadFinished = onWebLoaded,
        onDownloadStarted = onDownloadStarted,
        onUrlChanged = { url ->
            if (url.isNotBlank()) {
                onUrlChanged(url)
            }
        },
        modifier = modifier
    )
}


