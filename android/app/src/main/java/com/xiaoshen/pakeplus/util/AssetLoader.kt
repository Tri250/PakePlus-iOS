package com.xiaoshen.pakeplus.util

import android.content.Context
import java.io.IOException

object AssetLoader {

    fun loadAssetText(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            null
        }
    }
}
