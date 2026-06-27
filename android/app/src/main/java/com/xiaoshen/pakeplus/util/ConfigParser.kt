package com.xiaoshen.pakeplus.util

import android.os.Bundle
import com.xiaoshen.pakeplus.ManifestConfig

object ConfigParser {

    private const val DEFAULT_WEB_URL = "https://www.pakeplus.com/"

    fun parse(metaData: Bundle?): ManifestConfig {
        val bundle = metaData ?: Bundle()
        return ManifestConfig(
            webUrl = bundle.getString("WEB_URL", DEFAULT_WEB_URL),
            debug = bundle.getString("DEBUG", "false").toBooleanStrictOrNull() ?: false,
            fullscreen = bundle.getString("FULLSCREEN", "false").toBooleanStrictOrNull() ?: false,
            launchImage = bundle.getString("LAUNCH_IMAGE", "false").toBooleanStrictOrNull() ?: false,
            screenOn = bundle.getString("SCREEN_ON", "false").toBooleanStrictOrNull() ?: false,
            userAgent = bundle.getString("USER_AGENT", ""),
            isHtml = bundle.getString("IS_HTML", "false").toBooleanStrictOrNull() ?: false
        )
    }
}
