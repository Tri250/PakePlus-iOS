package com.xiaoshen.pakeplus.util

import android.os.Bundle
import com.xiaoshen.pakeplus.ManifestConfig

object ConfigParser {

    private const val DEFAULT_WEB_URL = "https://www.pakeplus.com/"

    fun parse(metaData: Bundle?): ManifestConfig {
        val bundle = metaData ?: Bundle()
        return ManifestConfig(
            webUrl = parseString(bundle, "WEB_URL", DEFAULT_WEB_URL),
            debug = parseBoolean(bundle, "DEBUG"),
            fullscreen = parseBoolean(bundle, "FULLSCREEN"),
            launchImage = parseBoolean(bundle, "LAUNCH_IMAGE"),
            screenOn = parseBoolean(bundle, "SCREEN_ON"),
            userAgent = parseString(bundle, "USER_AGENT", ""),
            isHtml = parseBoolean(bundle, "IS_HTML")
        )
    }

    fun parseBoolean(bundle: Bundle?, key: String, default: Boolean = false): Boolean {
        return parseString(bundle, key, default.toString()).toBooleanStrictOrNull() ?: default
    }

    fun parseString(bundle: Bundle?, key: String, default: String = ""): String {
        return bundle?.getString(key, default) ?: default
    }
}
