package com.xiaoshen.pakeplus.data

data class SafeArea(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
)

data class HeaderConfig(
    val show: Boolean = false,
    val title: String = "",
    val backgroundColor: String = "",
    val color: String = "",
    val fontSize: Int = 16,
    val fontWeight: String = "bold",
    val loading: Boolean = false,
    val toolBar: Boolean = false,
    val toolBarBackgroundColor: String = "",
    val toolBarColor: String = "",
    val toolBarFontSize: Int = 16,
    val toolBarFontWeight: String = "bold"
)

data class SiderMenuConfig(
    val show: Boolean = false,
    val width: Int = 0,
    val backgroundColor: String = "",
    val color: String = "",
    val fontSize: Int = 16,
    val fontWeight: String = "bold",
    val title: String = "",
    val titleColor: String = "",
    val titleFontSize: Int = 16,
    val titleFontWeight: String = "bold"
)

data class TabBarItem(
    val title: String = "",
    val icon: String = "",
    val url: String = ""
)

data class TabBarConfig(
    val show: Boolean = false,
    val backgroundColor: String = "",
    val color: String = "",
    val activeColor: String = "",
    val fontSize: Int = 16,
    val fontWeight: String = "bold",
    val tabBarItem: List<TabBarItem> = emptyList()
)

data class WebViewConfig(
    val userAgent: String = "",
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val allowFileAccess: Boolean = false,
    val loadWithOverviewMode: Boolean = true,
    val setSupportZoom: Boolean = false,
    val clearCache: Boolean = false
)

data class PhoneConfig(
    val fullScreen: Boolean = false,
    val launchImage: String = "",
    val screenOn: Boolean = false,
    val download: Boolean = true,
    val internet: Boolean = true,
    val position: Boolean = false,
    val direction: String = "default",
    val callPhone: Boolean = false,
    val microphone: Boolean = false,
    val camera: Boolean = false,
    val backgroundPlay: Boolean = false,
    val compress: Boolean = false,
    val appSize: Int = 0,
    val videoFull: Boolean = true,
    val author: String = "pakeplus",
    val startMethod: String = "none",
    val startPwd: String = "123456",
    val pwdTitle: String = "",
    val pwdBtn: String = "",
    val pwdPlace: String = "",
    val pwdTip: String = "",
    val pwdError: String = "",
    val endPwd: String = "123456",
    val endPwdTitle: String = "",
    val endPwdTip: String = "",
    val endPwdError: String = "",
    val pwdStyle: String = "flat",
    val pwdTheme: String = "dark",
    val safeArea: SafeArea = SafeArea(),
    val header: HeaderConfig = HeaderConfig(),
    val siderMenu: SiderMenuConfig = SiderMenuConfig(),
    val tabBar: TabBarConfig = TabBarConfig(),
    val webview: WebViewConfig = WebViewConfig()
)

data class AndroidConfig(
    val name: String = "PakePlus",
    val showName: String = "PakePlus",
    val version: String = "0.0.1",
    val webUrl: String = "https://www.pakeplus.com/",
    val id: String = "com.xiaoshen.pakeplus.android",
    val icon: String = "",
    val input: String = "",
    val output: String = "",
    val rounded: Boolean = true,
    val copyTo: String = "",
    val androidResDir: String = "",
    val desc: String = "",
    val pubBody: String = "",
    val isHtml: Boolean = false,
    val debug: Boolean = false,
    val safeArea: String = "all"
)

data class AppConfig(
    val android: AndroidConfig = AndroidConfig(),
    val phone: PhoneConfig = PhoneConfig()
)
