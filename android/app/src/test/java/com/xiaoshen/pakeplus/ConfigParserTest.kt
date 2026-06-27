package com.xiaoshen.pakeplus

import android.os.Bundle
import com.xiaoshen.pakeplus.util.ConfigLoader
import com.xiaoshen.pakeplus.util.ConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigParserTest {

    @Test
    fun `parse returns defaults for empty bundle`() {
        val config = ConfigParser.parse(Bundle())
        assertEquals("https://www.pakeplus.com/", config.webUrl)
        assertFalse(config.debug)
        assertFalse(config.fullscreen)
        assertFalse(config.launchImage)
        assertFalse(config.screenOn)
        assertEquals("", config.userAgent)
        assertFalse(config.isHtml)
    }

    @Test
    fun `parse returns defaults for null bundle`() {
        val config = ConfigParser.parse(null)
        assertEquals("https://www.pakeplus.com/", config.webUrl)
        assertFalse(config.debug)
    }

    @Test
    fun `parse reads all meta data values`() {
        val bundle = Bundle().apply {
            putString("WEB_URL", "https://juejin.cn/")
            putString("DEBUG", "true")
            putString("FULLSCREEN", "true")
            putString("LAUNCH_IMAGE", "true")
            putString("SCREEN_ON", "true")
            putString("USER_AGENT", "PakePlus/1.0")
            putString("IS_HTML", "true")
        }
        val config = ConfigParser.parse(bundle)
        assertEquals("https://juejin.cn/", config.webUrl)
        assertTrue(config.debug)
        assertTrue(config.fullscreen)
        assertTrue(config.launchImage)
        assertTrue(config.screenOn)
        assertEquals("PakePlus/1.0", config.userAgent)
        assertTrue(config.isHtml)
    }

    @Test
    fun `parse ignores malformed booleans and falls back to false`() {
        val bundle = Bundle().apply {
            putString("DEBUG", "not-a-boolean")
        }
        val config = ConfigParser.parse(bundle)
        assertFalse(config.debug)
    }

    @Test
    fun `ConfigLoader parses sample config json`() {
        val json = """
            {
                "android": {
                    "webUrl": "https://juejin.cn/",
                    "debug": true,
                    "isHtml": false
                },
                "phone": {
                    "fullScreen": true,
                    "screenOn": true,
                    "safeArea": { "top": 20, "bottom": 10, "left": 5, "right": 5 },
                    "header": { "show": true, "title": "PakePlus", "toolBar": true },
                    "siderMenu": { "show": true, "title": "左侧菜单" },
                    "tabBar": {
                        "show": true,
                        "tabBarItem": [
                            { "title": "首页", "icon": "home", "url": "https://a.com/" },
                            { "title": "收藏", "icon": "star", "url": "https://b.com/" }
                        ]
                    },
                    "webview": { "userAgent": "PakePlus/1.0", "javaScriptEnabled": false }
                }
            }
        """.trimIndent()
        val config = ConfigLoader.parseJson(json)
        assertEquals("https://juejin.cn/", config.android.webUrl)
        assertTrue(config.android.debug)
        assertFalse(config.android.isHtml)
        assertTrue(config.phone.fullScreen)
        assertTrue(config.phone.screenOn)
        assertEquals(20, config.phone.safeArea.top)
        assertEquals(10, config.phone.safeArea.bottom)
        assertTrue(config.phone.header.show)
        assertEquals("PakePlus", config.phone.header.title)
        assertTrue(config.phone.header.toolBar)
        assertTrue(config.phone.siderMenu.show)
        assertEquals("左侧菜单", config.phone.siderMenu.title)
        assertTrue(config.phone.tabBar.show)
        assertEquals(2, config.phone.tabBar.tabBarItem.size)
        assertEquals("首页", config.phone.tabBar.tabBarItem[0].title)
        assertEquals("https://b.com/", config.phone.tabBar.tabBarItem[1].url)
        assertEquals("PakePlus/1.0", config.phone.webview.userAgent)
        assertFalse(config.phone.webview.javaScriptEnabled)
    }

    @Test
    fun `ConfigLoader returns defaults for empty json`() {
        val config = ConfigLoader.parseJson("{}")
        assertEquals("https://www.pakeplus.com/", config.android.webUrl)
        assertFalse(config.phone.tabBar.show)
        assertTrue(config.phone.webview.javaScriptEnabled)
    }
}
