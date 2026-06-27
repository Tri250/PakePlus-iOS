package com.xiaoshen.pakeplus

import android.os.Bundle
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
}
