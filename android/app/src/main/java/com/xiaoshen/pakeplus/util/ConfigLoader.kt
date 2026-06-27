package com.xiaoshen.pakeplus.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.xiaoshen.pakeplus.data.AndroidConfig
import com.xiaoshen.pakeplus.data.AppConfig
import com.xiaoshen.pakeplus.data.HeaderConfig
import com.xiaoshen.pakeplus.data.PhoneConfig
import com.xiaoshen.pakeplus.data.SafeArea
import com.xiaoshen.pakeplus.data.SiderMenuConfig
import com.xiaoshen.pakeplus.data.TabBarConfig
import com.xiaoshen.pakeplus.data.TabBarItem
import com.xiaoshen.pakeplus.data.WebViewConfig
import org.json.JSONObject

object ConfigLoader {

    private const val ASSET_CONFIG = "config.json"

    fun load(context: Context): AppConfig {
        val assetsConfig = loadFromAssets(context)
        val manifestMeta = readManifestMeta(context)
        return mergeWithManifest(assetsConfig ?: AppConfig(), manifestMeta)
    }

    fun parseJson(json: String): AppConfig {
        return parseAppConfig(JSONObject(json))
    }

    private fun loadFromAssets(context: Context): AppConfig? {
        return try {
            context.assets.open(ASSET_CONFIG).bufferedReader().use { it.readText() }.let {
                parseAppConfig(JSONObject(it))
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readManifestMeta(context: Context): Bundle {
        return try {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            ).metaData ?: Bundle()
        } catch (_: Exception) {
            Bundle()
        }
    }

    private fun mergeWithManifest(config: AppConfig, meta: Bundle): AppConfig {
        val manifestConfig = ConfigParser.parse(meta)

        val android = config.android.copy(
            webUrl = manifestConfig.webUrl,
            debug = manifestConfig.debug,
            isHtml = manifestConfig.isHtml
        )

        val phone = config.phone.copy(
            fullScreen = manifestConfig.fullscreen,
            screenOn = manifestConfig.screenOn,
            launchImage = if (manifestConfig.launchImage) {
                config.phone.launchImage.takeIf { it.isNotBlank() } ?: "launch"
            } else "",
            webview = config.phone.webview.copy(
                userAgent = manifestConfig.userAgent
            )
        )

        return config.copy(android = android, phone = phone)
    }

    private fun parseAppConfig(json: JSONObject): AppConfig {
        return AppConfig(
            android = json.optJSONObject("android")?.let { parseAndroidConfig(it) } ?: AndroidConfig(),
            phone = json.optJSONObject("phone")?.let { parsePhoneConfig(it) } ?: PhoneConfig()
        )
    }

    private fun parseAndroidConfig(json: JSONObject): AndroidConfig {
        return AndroidConfig(
            name = json.optString("name", AndroidConfig().name),
            showName = json.optString("showName", AndroidConfig().showName),
            version = json.optString("version", AndroidConfig().version),
            webUrl = json.optString("webUrl", AndroidConfig().webUrl),
            id = json.optString("id", AndroidConfig().id),
            icon = json.optString("icon", AndroidConfig().icon),
            input = json.optString("input", AndroidConfig().input),
            output = json.optString("output", AndroidConfig().output),
            rounded = json.optBoolean("rounded", AndroidConfig().rounded),
            copyTo = json.optString("copyTo", AndroidConfig().copyTo),
            androidResDir = json.optString("androidResDir", AndroidConfig().androidResDir),
            desc = json.optString("desc", AndroidConfig().desc),
            pubBody = json.optString("pubBody", AndroidConfig().pubBody),
            isHtml = json.optBoolean("isHtml", AndroidConfig().isHtml),
            debug = json.optBoolean("debug", AndroidConfig().debug),
            safeArea = json.optString("safeArea", AndroidConfig().safeArea)
        )
    }

    private fun parsePhoneConfig(json: JSONObject): PhoneConfig {
        return PhoneConfig(
            fullScreen = json.optBoolean("fullScreen", PhoneConfig().fullScreen),
            launchImage = json.optString("launchImage", PhoneConfig().launchImage),
            screenOn = json.optBoolean("screenOn", PhoneConfig().screenOn),
            download = json.optBoolean("download", PhoneConfig().download),
            internet = json.optBoolean("internet", PhoneConfig().internet),
            position = json.optBoolean("position", PhoneConfig().position),
            direction = json.optString("direction", PhoneConfig().direction),
            callPhone = json.optBoolean("callPhone", PhoneConfig().callPhone),
            microphone = json.optBoolean("microphone", PhoneConfig().microphone),
            camera = json.optBoolean("camera", PhoneConfig().camera),
            backgroundPlay = json.optBoolean("backgroundPlay", PhoneConfig().backgroundPlay),
            compress = json.optBoolean("compress", PhoneConfig().compress),
            appSize = json.optInt("appSize", PhoneConfig().appSize),
            videoFull = json.optBoolean("videoFull", PhoneConfig().videoFull),
            author = json.optString("author", PhoneConfig().author),
            startMethod = json.optString("startMethod", PhoneConfig().startMethod),
            startPwd = json.optString("startPwd", PhoneConfig().startPwd),
            pwdTitle = json.optString("pwdTitle", PhoneConfig().pwdTitle),
            pwdBtn = json.optString("pwdBtn", PhoneConfig().pwdBtn),
            pwdPlace = json.optString("pwdPlace", PhoneConfig().pwdPlace),
            pwdTip = json.optString("pwdTip", PhoneConfig().pwdTip),
            pwdError = json.optString("pwdError", PhoneConfig().pwdError),
            endPwd = json.optString("endPwd", PhoneConfig().endPwd),
            endPwdTitle = json.optString("endPwdTitle", PhoneConfig().endPwdTitle),
            endPwdTip = json.optString("endPwdTip", PhoneConfig().endPwdTip),
            endPwdError = json.optString("endPwdError", PhoneConfig().endPwdError),
            pwdStyle = json.optString("pwdStyle", PhoneConfig().pwdStyle),
            pwdTheme = json.optString("pwdTheme", PhoneConfig().pwdTheme),
            safeArea = json.optJSONObject("safeArea")?.let { parseSafeArea(it) } ?: SafeArea(),
            header = json.optJSONObject("header")?.let { parseHeaderConfig(it) } ?: HeaderConfig(),
            siderMenu = json.optJSONObject("siderMenu")?.let { parseSiderMenuConfig(it) } ?: SiderMenuConfig(),
            tabBar = json.optJSONObject("tabBar")?.let { parseTabBarConfig(it) } ?: TabBarConfig(),
            webview = json.optJSONObject("webview")?.let { parseWebViewConfig(it) } ?: WebViewConfig()
        )
    }

    private fun parseSafeArea(json: JSONObject): SafeArea {
        return SafeArea(
            top = json.optInt("top", SafeArea().top),
            bottom = json.optInt("bottom", SafeArea().bottom),
            left = json.optInt("left", SafeArea().left),
            right = json.optInt("right", SafeArea().right)
        )
    }

    private fun parseHeaderConfig(json: JSONObject): HeaderConfig {
        return HeaderConfig(
            show = json.optBoolean("show", HeaderConfig().show),
            title = json.optString("title", HeaderConfig().title),
            backgroundColor = json.optString("backgroundColor", HeaderConfig().backgroundColor),
            color = json.optString("color", HeaderConfig().color),
            fontSize = json.optInt("fontSize", HeaderConfig().fontSize),
            fontWeight = json.optString("fontWeight", HeaderConfig().fontWeight),
            loading = json.optBoolean("loading", HeaderConfig().loading),
            toolBar = json.optBoolean("toolBar", HeaderConfig().toolBar),
            toolBarBackgroundColor = json.optString("toolBarBackgroundColor", HeaderConfig().toolBarBackgroundColor),
            toolBarColor = json.optString("toolBarColor", HeaderConfig().toolBarColor),
            toolBarFontSize = json.optInt("toolBarFontSize", HeaderConfig().toolBarFontSize),
            toolBarFontWeight = json.optString("toolBarFontWeight", HeaderConfig().toolBarFontWeight)
        )
    }

    private fun parseSiderMenuConfig(json: JSONObject): SiderMenuConfig {
        return SiderMenuConfig(
            show = json.optBoolean("show", SiderMenuConfig().show),
            width = json.optInt("width", SiderMenuConfig().width),
            backgroundColor = json.optString("backgroundColor", SiderMenuConfig().backgroundColor),
            color = json.optString("color", SiderMenuConfig().color),
            fontSize = json.optInt("fontSize", SiderMenuConfig().fontSize),
            fontWeight = json.optString("fontWeight", SiderMenuConfig().fontWeight),
            title = json.optString("title", SiderMenuConfig().title),
            titleColor = json.optString("titleColor", SiderMenuConfig().titleColor),
            titleFontSize = json.optInt("titleFontSize", SiderMenuConfig().titleFontSize),
            titleFontWeight = json.optString("titleFontWeight", SiderMenuConfig().titleFontWeight)
        )
    }

    private fun parseTabBarConfig(json: JSONObject): TabBarConfig {
        val items = json.optJSONArray("tabBarItem")?.let { array ->
            (0 until array.length()).mapNotNull { index ->
                (array.optJSONObject(index) as? JSONObject)?.let { parseTabBarItem(it) }
            }
        } ?: emptyList()
        return TabBarConfig(
            show = json.optBoolean("show", TabBarConfig().show),
            backgroundColor = json.optString("backgroundColor", TabBarConfig().backgroundColor),
            color = json.optString("color", TabBarConfig().color),
            activeColor = json.optString("activeColor", TabBarConfig().activeColor),
            fontSize = json.optInt("fontSize", TabBarConfig().fontSize),
            fontWeight = json.optString("fontWeight", TabBarConfig().fontWeight),
            tabBarItem = items
        )
    }

    private fun parseTabBarItem(json: JSONObject): TabBarItem {
        return TabBarItem(
            title = json.optString("title", TabBarItem().title),
            icon = json.optString("icon", TabBarItem().icon),
            url = json.optString("url", TabBarItem().url)
        )
    }

    private fun parseWebViewConfig(json: JSONObject): WebViewConfig {
        return WebViewConfig(
            userAgent = json.optString("userAgent", WebViewConfig().userAgent),
            javaScriptEnabled = json.optBoolean("javaScriptEnabled", WebViewConfig().javaScriptEnabled),
            domStorageEnabled = json.optBoolean("domStorageEnabled", WebViewConfig().domStorageEnabled),
            allowFileAccess = json.optBoolean("allowFileAccess", WebViewConfig().allowFileAccess),
            loadWithOverviewMode = json.optBoolean("loadWithOverviewMode", WebViewConfig().loadWithOverviewMode),
            setSupportZoom = json.optBoolean("setSupportZoom", WebViewConfig().setSupportZoom),
            clearCache = json.optBoolean("clearCache", WebViewConfig().clearCache)
        )
    }
}
